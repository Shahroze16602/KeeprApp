package com.systematics.app.utils.monetization

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.FirebaseAnalytics.ConsentType
import com.google.firebase.analytics.analytics
import com.google.firebase.analytics.logEvent
import com.google.firebase.analytics.setConsent
import com.systematics.monetization.admob.R
import com.systematics.monetization.admob.consent.GoogleConsentManager
import com.systematics.monetization.admob.integration.AdmobIntegration
import com.systematics.monetization.admob.models.natives.AdmobNativeLayoutRegistry
import com.systematics.monetization.admob.revenue.models.AdmobRevenue
import com.systematics.monetization.admob.ui.store.defaultAdmobNativeRegistryModel
import com.systematics.monetization.core.BuildConfig as AdsBuildConfig
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.analytics.AdsEvents
import com.systematics.monetization.core.analytics.AnalyticsEventsListener
import com.systematics.monetization.core.interfaces.InitializationStatus
import com.systematics.monetization.core.interfaces.MonetizationStateListener
import com.systematics.monetization.core.managers.consent.ConsentStatus
import com.systematics.monetization.core.models.AdsNetworkModel
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.models.enums.AdsNetworkInitStrategy
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.revenue.interfaces.RevenueListener
import com.systematics.monetization.core.revenue.models.RevenueModel
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.APP_ID_TEST
import com.systematics.monetization.core.utils.addRegistry
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.ad.FullScreenAdShowManager
import com.systematics.monetization.ui.di.MonetizationDIContainer
import com.systematics.monetization.ui.remote.MonetizationRemote
import com.systematics.monetization.ui.remote.config.AdsUiRemoteConfigs
import com.systematics.monetization.core.utils.InternetController
import com.systematics.monetization.ui.utils.MonetizationConditionalsState
import com.systematics.monetization.ui.utils.MonetizationSharedState
import com.systematics.monetization.ui.utils.applyMapping
import com.systematics.monetization.ui.utils.monetizationGet
import com.systematics.app.BuildConfig
import com.systematics.app.utils.monetization.config.frontend.config.AdsPlacement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

class MonetizationHandler(
    private val application: Application,
    private val internetController: InternetController,
    private val monetizationRemote: MonetizationRemote,
    private val adsCoreRemoteConfigs: AdsCoreRemoteConfigs,
    private val adsUiRemoteConfigs: AdsUiRemoteConfigs
) : Application.ActivityLifecycleCallbacks {

    private val consentManager = GoogleConsentManager()
    private lateinit var monetizationApp: MonetizationApp

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var currentActivity: Activity? = null

    private var initializationStatus: InitializationStatus = InitializationStatus.Uninitialized

    private var alreadyDeferred = false

    private val listeners = mutableSetOf<MonetizationStateListener>()

    private fun addListener(listener: MonetizationStateListener) {
        listeners.add(listener)
    }

    private fun removeListener(listener: MonetizationStateListener) {
        listeners.remove(listener)
    }

    fun observeInitialization(): Flow<InitializationStatus> = callbackFlow {
        val listener = object : MonetizationStateListener {
            override fun onInitializationStatusUpdated(status: InitializationStatus) {
                trySend(status)
            }
        }
        trySend(initializationStatus)
        addListener(listener)
        awaitClose { removeListener(listener) }
    }

    fun emit() {
        listeners.forEach { it.onInitializationStatusUpdated(initializationStatus) }
    }

    fun setupMonetization(application: Application) {
        application.registerActivityLifecycleCallbacks(this)
        monetizationApp = MonetizationApp(
            context = application,
            internetController = internetController,
            adsCoreRemoteConfigs = adsCoreRemoteConfigs
        )
        MonetizationDIContainer.init(
            internetController = internetController,
            monetizationRemote = monetizationRemote,
            monetizationApp = monetizationApp,
            adsCoreRemoteConfigs = adsCoreRemoteConfigs,
            adsUiRemoteConfigs = adsUiRemoteConfigs
        )
        monetizationApp.eventsListener = object : AnalyticsEventsListener {
            override fun onEvent(adsEvents: AdsEvents) {
                if (!AdsBuildConfig.DEBUG) {
                    adsEvents.params?.let {
                        val bundle = Bundle().apply { it() }
                        Firebase.analytics.logEvent(adsEvents.name, bundle)
                    } ?: run {
                        Firebase.analytics.logEvent(adsEvents.name, null)
                    }
                } else {
                    Log.d(TAG, "onEvent: $adsEvents")
                }
            }
        }
    }

    fun initMonetizationOnActivity(activity: Activity) {
        if (alreadyDeferred) {
            Log.d(TAG, "initMonetizationOnActivity: already deferred")
            return
        }
        Log.d(TAG, "initMonetizationOnActivity: initializing now")
        initMonetization(
            activity = activity,
            CoroutineScope(Dispatchers.Default + SupervisorJob()),
            defferOnFail = true,
            onDone = {
                Log.d(TAG, "initMonetizationOnActivity: initialization updated $it")
                emit()
            }
        )
    }


    fun initMonetization(
        activity: Activity,
        coroutineScope: CoroutineScope,
        defferOnFail: Boolean = true,
        onDone: (Boolean) -> Unit = {}
    ) {
        initializationStatus = InitializationStatus.Initializing
        if (!internetController.isInternetConnected) {
            if (defferOnFail && !alreadyDeferred) {
                ALog.d(TAG, "initMonetization: No internet")
                defersInitialization()
                alreadyDeferred = true
                initializationStatus = InitializationStatus.Deferred
            }
            onDone(false)
            return
        }
        val onDoneInternal: (Boolean) -> Unit = {
            Log.d(TAG, "initMonetization: $it")
            initializationStatus =
                if (it) InitializationStatus.Success else InitializationStatus.Failed
            onDone(it)
        }

        val onConsentFailed = {
            val isConnected = internetController.isInternetConnected
            onDoneInternal(false)
            if (!isConnected && defferOnFail && !alreadyDeferred) {
                Log.d(TAG, "initMonetization: No internet")
                defersInitialization()
                alreadyDeferred = true
                initializationStatus = InitializationStatus.Deferred
            }
        }

        coroutineScope.launch(Dispatchers.IO) {
            if (defferOnFail) {
                delay(750)
            }
            consentManager.show(activity) { consentStatus ->
                when (consentStatus) {
                    ConsentStatus.CHECKING -> Unit
                    ConsentStatus.GRANTED -> {
                        Firebase.analytics.setConsent {
                            ConsentType.AD_STORAGE to FirebaseAnalytics.ConsentStatus.GRANTED
                            ConsentType.AD_USER_DATA to FirebaseAnalytics.ConsentStatus.GRANTED
                            ConsentType.AD_PERSONALIZATION to FirebaseAnalytics.ConsentStatus.GRANTED
                            ConsentType.ANALYTICS_STORAGE to FirebaseAnalytics.ConsentStatus.GRANTED
                        }
                        coroutineScope.launch {
                            initializeMonetization(getAdsNetworkModel())
                            monetizationGet<MonetizationInstall>().updateFromRemote()
                            onDoneInternal(true)
                        }
                        passConsent()
                    }

                    ConsentStatus.FAILED -> {
                        Log.d(TAG, "initMonetization: consent failed")
                        onConsentFailed()
                    }
                }
            }
        }
    }

    fun onAppResumed() {
        currentActivity?.let {
            Log.d(TAG, "onAppResumed: current activity name: ${it.javaClass.simpleName}")
            if (!MonetizationSharedState.blockAppOpenAd.value) {
                if (!it.isDestroyed) {
                    showAppOpen(it)
                } else {
                    Log.d(TAG, "onAppResumed: ad on destroy")
                }
            } else {
                Log.d(TAG, "onAppResumed: app open blocked")
                if (MonetizationSharedState.unblockAppOpenAdOnAppResume) {
                    Log.d(TAG, "onAppResumed: app open unblocked on resume")
                    MonetizationSharedState.unblockAppOpenOnResume()
                }
                Unit
            }
        } ?: run {
            Log.d(TAG, "onStart: no activity")
        }
    }

    private fun defersInitialization() {
        Log.d(TAG, "defersInitialization: ")
        var job: Job? = null
        job = coroutineScope.launch {
            internetController.observeInternet().collect {
                if (it) {
                    Log.d(TAG, "defersInitialization: internet resumed")
                    currentActivity?.let { activity ->
                        coroutineScope.launch {
                            initMonetizationAgain(activity)
                        }
                    }
                    job?.cancel()
                }
            }
        }
    }

    private fun initMonetizationAgain(activity: Activity) {
        Log.d(TAG, "initMonetizationAgain: starting on activity: $activity")
        initMonetization(
            activity = activity,
            coroutineScope = coroutineScope,
            defferOnFail = false,
            onDone = {
                if (it) {
                    monetizationGet<MonetizationInstall>().deferredInstall()
                }
                emit()
            }
        )
    }

    private suspend fun initializeMonetization(
        adsNetworkModels: List<AdsNetworkModel>
    ) {
        ALog.d(TAG, "initializing monetization ${adsNetworkModels.size} networks")
        if (adsNetworkModels.isEmpty()) {
            ALog.d(TAG, "no networks for monetization")
            return
        }
        monetizationApp.initializeMonetization(
            adsNetworkModels = adsNetworkModels,
            mapAdInfo = ::appAdsMappings
        )
    }

    private fun getAdsNetworkModel(): List<AdsNetworkModel> {

        val revenueListener = object : RevenueListener {
            override fun <T : RevenueModel> onRevenue(revenueModel: T) {
                when (revenueModel) {
                    is AdmobRevenue -> {
                        // Sending to Firebase custom event
                        val revenue = revenueModel.adValue.valueMicros / 1_000_000.0
                        Firebase.analytics.logEvent("admob_revenue") {
                            param(FirebaseAnalytics.Param.AD_PLATFORM, "admob")
                            param(
                                FirebaseAnalytics.Param.AD_UNIT_NAME,
                                revenueModel.adUnitId.orEmpty()
                            )
                            param(
                                FirebaseAnalytics.Param.AD_FORMAT,
                                revenueModel.adType.orEmpty()
                            )
                            param(
                                FirebaseAnalytics.Param.AD_SOURCE,
                                "unknown"
                            )
                            param(FirebaseAnalytics.Param.VALUE, revenue)
                            param(
                                FirebaseAnalytics.Param.CURRENCY,
                                revenueModel.adValue.currencyCode
                            )
                        }
                    }
                }
            }
        }

        val admobNetworkIntegration = AdmobIntegration(
            appId = if (BuildConfig.DEBUG) APP_ID_TEST else ADMOB_APP_ID,
            context = application,
            admobNativeRegistry = defaultAdmobNativeRegistryModel.addRegistry(
                "ad_unified",
                AdmobNativeLayoutRegistry(R.layout.ad_unified)
            )
        ).apply {
            attachRevenueListener(revenueListener)
        }
        return listOf(
            AdsNetworkModel(admobNetworkIntegration, AdsNetworkInitStrategy.EAGERLY)
        )
    }

    private fun passConsent() {

    }

    private fun appAdsMappings(adInfo: AdInfo): AdInfo {
        val trueConditions = monetizationRemote.appAdsMappings.appAdsConditionalMappings.filter {
            MonetizationConditionalsState.isConditionTrue(it.key)
        }.values
        if (trueConditions.isEmpty()) {
            return adInfo
        }
        var latestMapping = adInfo
        trueConditions.forEach { rules ->
            var internalMapping = latestMapping
            rules.forEach { rule ->
                internalMapping = applyMapping(internalMapping, rule)
            }
            latestMapping = internalMapping
        }
        if (adInfo != latestMapping) {
            Log.d(TAG, "appAdsMappings: mapped $adInfo to $latestMapping")
        }
        return latestMapping
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
        Log.d("AppActivityLifecycle", "onActivityCreated: $p0")
        if (initializationStatus == InitializationStatus.Uninitialized) {
            initMonetizationOnActivity(activity = p0)
        }
    }

    override fun onActivityStarted(p0: Activity) {
        Log.d("AppActivityLifecycle", "onActivityStarted: $p0")
    }

    override fun onActivityResumed(p0: Activity) {
        Log.d("AppActivityLifecycle", "onActivityResumed: $p0")
        currentActivity = p0
    }

    override fun onActivityPaused(p0: Activity) {
        Log.d("AppActivityLifecycle", "onActivityPaused: $p0")
    }

    override fun onActivityStopped(p0: Activity) {
        Log.d("AppActivityLifecycle", "onActivityStopped: $p0")
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        Log.d("AppActivityLifecycle", "onActivitySaveInstanceState: $p0")
    }

    override fun onActivityDestroyed(p0: Activity) {
        Log.d("AppActivityLifecycle", "onActivityDestroyed: $p0")
        if (currentActivity == p0) {
            currentActivity = null
        }
    }

    private fun showAppOpen(
        activity: Activity,
        placementKey: String = AdsPlacement.FullScreens.APP_OPEN_SWITCH,
        lifecycle: Lifecycle = ProcessLifecycleOwner.get().lifecycle,
        onDone: (Boolean) -> Unit = {}
    ) {
        monetizationGet<FullScreenAdShowManager>().showFullScreenAd(
            lifecycle = lifecycle,
            activity = activity,
            placementKey = placementKey,
            onAdShowing = {
                MonetizationSharedState.showDialog("appOpenBg")
            },
            onDone = {
                MonetizationSharedState.hideDialog()
                onDone(it)
            }
        )
    }

    companion object {

        private const val TAG = "MonetizationHandler"
        // Replace with the production AdMob application ID for release builds.
        private const val ADMOB_APP_ID = ""
    }
}
