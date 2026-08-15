package com.systematics.app

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.systematics.billing.core.data.datasource.PremiumHandler
import com.systematics.billing.core.di.BillingDIContainer
import com.systematics.billing.core.domain.model.PremiumState
import com.systematics.billing.core.domain.usecase.BuyPremiumOfferUseCase
import com.systematics.billing.core.domain.usecase.ObservePremiumStateUseCase
import com.systematics.billing.core.domain.usecase.QueryPremiumOffersUseCase
import com.systematics.billing.core.domain.usecase.QueryPurchasedOfferUseCase
import com.systematics.billing.core.utils.ResultResource
import com.systematics.billing.revenuecat.models.RevenueCatSubscribedOffer
import com.systematics.monetization.ui.compose.AdClosePurchaseBridge
import com.systematics.app.utils.core.AppLocaleManager
import com.systematics.app.utils.monetization.MonetizationHandler
import com.systematics.app.di.appModule
import com.systematics.app.di.dataModule
import com.systematics.app.di.domainModule
import com.systematics.app.di.frameworkModule
import com.systematics.app.di.monetizationModule
import com.systematics.app.di.presentationModule
import com.systematics.app.domain.model.PremiumOfferType
import com.systematics.app.domain.model.ThemeMode
import com.systematics.app.domain.usecase.GetPremiumOfferIdUseCase
import com.systematics.app.domain.usecase.GetStoredLanguageUseCase
import com.systematics.app.domain.usecase.GetThemeModeUseCase
import com.systematics.app.domain.usecase.IsAdsEnabledUseCase
import com.systematics.app.domain.usecase.IsBillingEnabledUseCase
import com.systematics.app.domain.usecase.MarkPremiumStatusResolvedUseCase
import com.systematics.app.domain.usecase.ObserveRemoteConfigInitializedUseCase
import com.systematics.app.domain.usecase.SetAdRemovalPurchasedUseCase
import com.systematics.app.domain.usecase.SetPremium1PurchasedUseCase
import com.systematics.app.domain.usecase.SetPremium2PurchasedUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AppClass : Application() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var processStarted = false
    private val premiumHandler: PremiumHandler by inject()
    private val monetizationHandler: MonetizationHandler by inject()
    private val getPremiumOfferId: GetPremiumOfferIdUseCase by inject()
    private val queryPurchasedOffer: QueryPurchasedOfferUseCase by inject()
    private val observePremiumState: ObservePremiumStateUseCase by inject()
    private val queryPremiumOffers: QueryPremiumOffersUseCase by inject()
    private val buyPremiumOffer: BuyPremiumOfferUseCase by inject()
    private val getStoredLanguage: GetStoredLanguageUseCase by inject()
    private val getThemeMode: GetThemeModeUseCase by inject()
    private val isAdsEnabled: IsAdsEnabledUseCase by inject()
    private val isBillingEnabled: IsBillingEnabledUseCase by inject()
    private val observeRemoteConfigInitialized: ObserveRemoteConfigInitializedUseCase by inject()
    private val setPremium1Purchased: SetPremium1PurchasedUseCase by inject()
    private val setPremium2Purchased: SetPremium2PurchasedUseCase by inject()
    private val setAdRemovalPurchased: SetAdRemovalPurchasedUseCase by inject()
    private val markPremiumStatusResolved: MarkPremiumStatusResolvedUseCase by inject()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            FirebaseApp.initializeApp(this)
            FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(false)
            FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled = false
        }

        startKoin {
            androidContext(this@AppClass)
            modules(
                appModule,
                dataModule,
                domainModule,
                frameworkModule,
                monetizationModule,
                presentationModule
            )
        }

        AppLocaleManager.applyLocale(getStoredLanguage())
        AppCompatDelegate.setDefaultNightMode(getThemeMode().nightMode)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                Log.d(TAG, "onStart: ")
                if (!processStarted) {
                    setupApp()
                    processStarted = true
                } else {
                    Log.d(TAG, "onStart: app resumed")
                    if (isAdsEnabled()) {
                        monetizationHandler.onAppResumed()
                    }
                }
            }
        })
    }

    private fun setupApp() {
        Log.d(TAG, "setupApp: ")
        coroutineScope.launch {
            observeRemoteConfigInitialized().first { it }
            if (isAdsEnabled()) {
                monetizationHandler.setupMonetization(this@AppClass)
            }
            if (isBillingEnabled()) {
                setupBilling()
            } else {
                markPremiumStatusResolved()
            }
        }
    }

    private fun setupBilling() {
        BillingDIContainer.init(premiumHandler)
        setupAdCloseToPurchase()
        coroutineScope.launch { queryPurchasedOffer.invoke() }

        coroutineScope.launch {
            coroutineScope.launch {
                Log.d(TAG, "setupBilling: added default timeout for premium status")
                delay(PREMIUM_RESOLVE_TIMEOUT_MS)
                markPremiumStatusResolved()
            }
            observePremiumState.invoke().collectLatest { premiumState ->
                when (premiumState) {
                    PremiumState.Checking -> {
                        Log.d(TAG, "onCreate: Checking")
                    }

                    PremiumState.NonPremium -> {
                        setPremium1Purchased(false)
                        setPremium2Purchased(false)
                        setAdRemovalPurchased(false)
                        markPremiumStatusResolved()
                        Log.d(TAG, "onCreate: NonPremium")
                    }

                    PremiumState.Unknown -> {
                        Log.d(TAG, "onCreate: Unknown")
                    }

                    is PremiumState.Error -> {
                        markPremiumStatusResolved()
                        Log.d(TAG, "onCreate: Error ${premiumState.message}")
                    }

                    is PremiumState.Premium -> {
                        Log.d(TAG, "onCreate: Premium Offer ${premiumState.subscribedOffer}")

                        val subscribedOffer =
                            premiumState.subscribedOffer as RevenueCatSubscribedOffer
                        val productIds = subscribedOffer.entitlements.map { it.identifier }

                        val premium1OfferId =
                            productIds.find { it == getPremiumOfferId(PremiumOfferType.PREMIUM_1) }
                        val premium2OfferId =
                            productIds.find { it == getPremiumOfferId(PremiumOfferType.PREMIUM_2) }
                        val premiumAdsRemoveOfferId =
                            productIds.find { it == getPremiumOfferId(PremiumOfferType.REMOVE_ADS) }

                        val premium1Purchased = premium1OfferId != null
                        val premium2Purchased = premium2OfferId != null
                        val premiumAdsRemovePurchased = premiumAdsRemoveOfferId != null

                        setPremium1Purchased(premium1Purchased)
                        setPremium2Purchased(premium2Purchased)
                        setAdRemovalPurchased(premiumAdsRemovePurchased)
                        markPremiumStatusResolved()
                        Log.d(TAG, "onCreate: Premium $productIds")
                    }
                }
            }
        }
    }

    private fun setupAdCloseToPurchase() {
        AdClosePurchaseBridge.onAdCloseClicked = { activity ->
            var launched = false
            coroutineScope.launch {
                val adRemoveId = getPremiumOfferId(PremiumOfferType.REMOVE_ADS)
                queryPremiumOffers.invoke(
                    subscriptionIds = listOf(adRemoveId),
                    productIds = listOf()
                ).collectLatest { latestState ->
                    if (!launched && latestState is ResultResource.Success) {
                        val offer = latestState.data?.find { it.id == adRemoveId }
                        if (offer != null) {
                            launched = true
                            launch(Dispatchers.Main) {
                                buyPremiumOffer.invoke(activity, offer) { success ->
                                    if (success) {
                                        setAdRemovalPurchased(true)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "AppClassTAG"

        private const val PREMIUM_RESOLVE_TIMEOUT_MS = 8000L
    }
}

private val ThemeMode.nightMode: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }
