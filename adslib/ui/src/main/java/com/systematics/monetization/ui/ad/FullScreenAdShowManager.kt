package com.systematics.monetization.ui.ad

import android.app.Activity
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.interfaces.AdShowResponse
import com.systematics.monetization.core.models.AdRequesterLoaded
import com.systematics.monetization.core.models.AdRequesterWaited
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.InternetController
import com.systematics.monetization.ui.AppAdsCounter
import com.systematics.monetization.ui.AppAdsTimer
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.ad.models.AdsShowRequestModel
import com.systematics.monetization.ui.exceptions.AdCounterShortException
import com.systematics.monetization.ui.exceptions.AdInstantLoadNoInternetException
import com.systematics.monetization.ui.exceptions.AdInstantLoadTimeoutException
import com.systematics.monetization.ui.exceptions.AdNotPreloadedException
import com.systematics.monetization.ui.exceptions.AdPlacementNotAllowedException
import com.systematics.monetization.ui.exceptions.AdPlacementNotFoundException
import com.systematics.monetization.ui.exceptions.AdTimerShortException
import com.systematics.monetization.ui.exceptions.MonetizationDisabledException
import com.systematics.monetization.ui.placement.models.FullScreenPlacementModel
import com.systematics.monetization.ui.remote.MonetizationRemote
import com.systematics.monetization.ui.utils.MonetizationSharedState
import com.systematics.monetization.ui.utils.counterOnLifecycle
import com.systematics.monetization.ui.utils.monetizationInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FullScreenAdShowManager(
    private val monetizationApp: MonetizationApp,
    private val monetizationInstall: MonetizationInstall,
    private val internetController: InternetController,
    private val adsCoreRemoteConfigs: AdsCoreRemoteConfigs
) {

    private val monetizationRemote: MonetizationRemote by monetizationInject()
    private val appAdsCounter: AppAdsCounter by monetizationInject()
    private val appAdsTimer: AppAdsTimer by monetizationInject()

    private val adClient = monetizationApp.adClient

    fun showFullScreenAd(
        lifecycle: Lifecycle,
        coroutineScope: CoroutineScope = lifecycle.coroutineScope,
        activity: Activity,
        placementKey: String,
        onPlacementData: (FullScreenPlacementModel) -> Unit = {},
        onAdLoaded: () -> Unit = {},
        onAdShowing: () -> Unit = {},
        onDone: (Boolean) -> Unit = {},
    ) {
        showFullScreenAd(
            lifecycle = lifecycle,
            coroutineScope = coroutineScope,
            activity = activity,
            placementKey = placementKey,
            onPlacementData = onPlacementData,
            onAdLoaded = onAdLoaded,
            onAdShowing = onAdShowing,
            onAdShown = { onDone(true) },
            onFailed = { onDone(false) }
        )
    }

    fun showFullScreenAd(
        lifecycle: Lifecycle,
        coroutineScope: CoroutineScope = lifecycle.coroutineScope,
        activity: Activity,
        placementKey: String,
        onPlacementData: (FullScreenPlacementModel) -> Unit = {},
        onAdLoaded: () -> Unit = {},
        onAdShowing: () -> Unit = {},
        onAdShown: () -> Unit = {},
        onFailed: (Exception) -> Unit = {}
    ) {
        if (!adsCoreRemoteConfigs.monetizationEnabled()) {
            Log.d(TAG, "monetization disabled for placement $placementKey")
            onFailed(MonetizationDisabledException())
            return
        }
        if (!monetizationApp.isInitialized.value) {
            Log.d(TAG, "showFullScreenAd: not initialized yet for placement $placementKey")
            onFailed(Exception("Ads library not initialized exception"))
            return
        }
        if (adsCoreRemoteConfigs.isPlacementOffForcefully(placementKey)) {
            ALog.d(TAG, "showFullScreenAd: forcefully off for placement $placementKey")
            onFailed(Exception("Placement forcefully off exception"))
            return
        }
        val placement = monetizationRemote.appAdsUiConfig.appFullScreens[placementKey]
        if (placement == null) {
            ALog.d(TAG, "No such placement for $placementKey")
            onFailed(AdPlacementNotFoundException("No such placement for $placementKey"))
            return
        }
        onPlacementData(placement)
        if (!placement.isEnabled) {
            ALog.d(TAG, "$placementKey placement not allowed")
            onFailed(AdPlacementNotAllowedException("${placement.tag} placement not allowed"))
            return
        }

        if (placement.counter.isNotEmpty()) {
            val isReached = appAdsCounter.isCounterReachedOnIncrement(placement.counter, true)
            if (!isReached) {
                ALog.d(TAG, "${placement.tag} counter ${placement.counter} short")
                onFailed(AdCounterShortException("${placement.tag} counter ${placement.counter} short"))
                return
            }
        }

        if (placement.timer.isNotEmpty()) {
            val isReached = appAdsTimer.isTimerReached(placement.timer)
            if (!isReached) {
                ALog.d(TAG, "${placement.tag} timer ${placement.timer} short")
                onFailed(AdTimerShortException("${placement.tag} timer ${placement.timer} short"))
                return
            }
        }

        try {
            showFullScreenAd(
                placement = placement,
                coroutineScope = coroutineScope,
                lifecycle = lifecycle,
                activity = activity,
                onFullScreenAdLoaded = onAdLoaded,
                onFullScreenAdShowing = onAdShowing,
                onFullScreenAdShown = {
                    onAdShown()
                    if (placement.timer.isNotEmpty()) {
                        appAdsTimer.startTimerForPlacement(placement.timer, isUtilized = true)
                    }
                },
                onAdFailed = onFailed
            )
        } catch (ex: Exception) {
            Log.e(TAG, "${placement.tag} failed to show ad", ex)
            onFailed(ex)
        }
    }

    private fun showFullScreenAd(
        placement: FullScreenPlacementModel,
        coroutineScope: CoroutineScope,
        lifecycle: Lifecycle,
        activity: Activity,
        onFullScreenAdLoaded: () -> Unit,
        onFullScreenAdShowing: () -> Unit,
        onFullScreenAdShown: () -> Unit,
        onAdFailed: (Exception) -> Unit
    ) {
        val consumables = placement.consumableAdGroups.filter { adClient.isAdLoaded(it) }
        val consumableRequests = consumables.map { AdsShowRequestModel(it, false) }

        val currentAdGroupType = placement.adGroupType
        val currentLoaded = adClient.isAdLoaded(currentAdGroupType)

        val loadedBackups = placement.backupAdGroups.filter { adClient.isAdLoaded(it.adGroupType) }
        val instantBackups = placement.backupAdGroups - loadedBackups

        val tryRequests = mutableListOf<AdsShowRequestModel>().apply {
            addAll(consumableRequests)
        }.apply {
            if (placement.isInstantAllowed) {
                add(AdsShowRequestModel(currentAdGroupType, true))
            } else {
                if (currentLoaded) {
                    add(AdsShowRequestModel(currentAdGroupType, false))
                }
            }
            addAll(
                loadedBackups.map { AdsShowRequestModel(it.adGroupType, it.isInstantAllowed) }
            )
            addAll(
                instantBackups.map { AdsShowRequestModel(it.adGroupType, it.isInstantAllowed) }
            )
        }

        if (!placement.isInstantAllowed && tryRequests.isEmpty()) {
            ALog.d(TAG, "${placement.tag} failed to show, no preloaded ad")
            throw AdNotPreloadedException("${placement.tag} failed to show, no preloaded ad")
        }

        Log.d(
            TAG,
            "${placement.tag} showFullScreenAd: consumables = $consumables, adGroupType = ${currentAdGroupType}, backupAdGroups = ${placement.backupAdGroups}"
        )

        showFullScreenAd(
            showRequestModels = tryRequests,
            adTag = placement.tag,
            showLoadingDialog = placement.showLoadingDialog,
            loadingDialogTimeMillis = placement.loadingDialogTimeMillis,
            showInstantLoadingDialog = placement.showInstantLoadingDialog,
            instantTimeoutSeconds = placement.instantTimeoutSeconds,
            dialogType = placement.dialogType,
            coroutineScope = coroutineScope,
            lifecycle = lifecycle,
            activity = activity,
            onFullScreenAdLoaded = onFullScreenAdLoaded,
            onFullScreenAdShowing = onFullScreenAdShowing,
            onFullScreenAdShown = onFullScreenAdShown,
            onAdFailed = onAdFailed
        )
    }

    private fun showFullScreenAd(
        showRequestModels: List<AdsShowRequestModel>,
        adTag: String,
        showLoadingDialog: Boolean,
        loadingDialogTimeMillis: Long,
        showInstantLoadingDialog: Boolean,
        dialogType: String,
        instantTimeoutSeconds: Long,
        coroutineScope: CoroutineScope,
        lifecycle: Lifecycle,
        activity: Activity,
        onFullScreenAdLoaded: () -> Unit,
        onFullScreenAdShowing: () -> Unit,
        onFullScreenAdShown: () -> Unit,
        onAdFailed: (Exception) -> Unit
    ) {
        val remainingShowRequests = showRequestModels.toMutableList()
        val currentShowRequest = MutableStateFlow(remainingShowRequests.first())

        coroutineScope.launch {
            currentShowRequest.collectLatest { currentRequest ->
                val isCurrentLoaded = adClient.isAdLoaded(currentRequest.adGroupType)
                val currentAllowed = if (isCurrentLoaded) {
                    true
                } else {
                    if (currentRequest.isInstantAllowed) {
                        monetizationInstall.loadAd(currentRequest.adGroupType)
                        true
                    } else {
                        false
                    }
                }
                if (currentAllowed) {
                    ALog.d(TAG, "$adTag placement requesting ${currentRequest.adGroupType}")
                    showFullScreenAd(
                        adGroupType = currentRequest.adGroupType,
                        adTag = adTag,
                        isInstantAd = currentRequest.isInstantAllowed && !isCurrentLoaded,
                        showLoadingDialog = showLoadingDialog,
                        loadingDialogTimeMillis = loadingDialogTimeMillis,
                        showInstantLoadingDialog = showInstantLoadingDialog,
                        dialogType = dialogType,
                        instantTimeoutSeconds = instantTimeoutSeconds,
                        coroutineScope = this,
                        lifecycle = lifecycle,
                        activity = activity,
                        onFullScreenAdLoaded = onFullScreenAdLoaded,
                        onFullScreenAdShowing = onFullScreenAdShowing,
                        onFullScreenAdShown = onFullScreenAdShown,
                        onAdFailed = {
                            ALog.d(
                                TAG,
                                "$adTag placement failed to show ${currentRequest.adGroupType}"
                            )
                            if (remainingShowRequests.isNotEmpty()) {
                                remainingShowRequests.removeAt(0)
                            }
                            if (remainingShowRequests.isNotEmpty()) {
                                currentShowRequest.value = remainingShowRequests.first()
                            } else {
                                Log.e(TAG, "$adTag placement failed to show", it)
                                onAdFailed(it)
                            }
                        }
                    )
                } else {
                    ALog.d(TAG, "$adTag placement failed to show ${currentRequest.adGroupType}")
                    if (remainingShowRequests.isNotEmpty()) {
                        remainingShowRequests.removeAt(0)
                    }
                    if (remainingShowRequests.isNotEmpty()) {
                        currentShowRequest.value = remainingShowRequests.first()
                    } else {
                        ALog.d(TAG, "$adTag placement failed to show")
                        onAdFailed(AdNotPreloadedException("No preloaded ad for $adTag"))
                    }
                }
            }
        }
    }

    private fun showFullScreenAd(
        adGroupType: String,
        adTag: String,
        isInstantAd: Boolean,
        showLoadingDialog: Boolean,
        loadingDialogTimeMillis: Long,
        showInstantLoadingDialog: Boolean,
        dialogType: String,
        instantTimeoutSeconds: Long,
        coroutineScope: CoroutineScope,
        lifecycle: Lifecycle,
        activity: Activity,
        onFullScreenAdLoaded: () -> Unit,
        onFullScreenAdShowing: () -> Unit,
        onFullScreenAdShown: () -> Unit,
        onAdFailed: (Exception) -> Unit
    ) {
        var instantAdTimerJob: Job? = null
        val adResponse = object : AdShowResponse {
            override suspend fun beforeAdShow() {
                MonetizationSharedState.apply {
                    blockAppOpen()
                    if (!isInstantAd) {
                        if (showLoadingDialog) {
                            showLoadingForTimeMillis(loadingDialogTimeMillis, dialogType)
                        }
                    } else {
                        if (showInstantLoadingDialog) {
                            hideDialog()
                        } else {
                            if (showLoadingDialog) {
                                showLoadingForTimeMillis(loadingDialogTimeMillis, dialogType)
                            }
                        }
                        instantAdTimerJob?.cancel()
                    }
                }
                onFullScreenAdShowing()
            }

            override fun getCoroutineScope(): CoroutineScope {
                onFullScreenAdLoaded()
                return coroutineScope
            }

            override fun getLifeLifecycle(): Lifecycle = lifecycle
            override fun getShowingActivity(): Activity = activity

            override fun onAdShown() {
                Log.d(TAG, "onAdShown: $adGroupType shown for $adTag")
                MonetizationSharedState.unblockAppOpen()
                onFullScreenAdShown()
            }

            override fun adShowingFailed(ex: Exception) {
                Log.e(TAG, "adShowingFailed: $adTag", ex)
                instantAdTimerJob?.cancel()
                MonetizationSharedState.apply {
                    hideDialog()
                    unblockAppOpen()
                }
                onAdFailed(ex)
            }
        }
        val requester = if (isInstantAd) {
            AdRequesterWaited(
                adGroupType = adGroupType,
                adTag = adTag,
                adResponse = adResponse
            )
        } else {
            AdRequesterLoaded(
                adGroupType = adGroupType,
                adTag = adTag,
                adResponse = adResponse
            )
        }
        if (requester is AdRequesterWaited) {
            if (internetController.isInternetConnected) {
                MonetizationSharedState.apply {
                    if (showInstantLoadingDialog) {
                        showDialog(dialogType)
                    }
                    blockAppOpen()
                }
                instantAdTimerJob = coroutineScope.launch {
                    counterOnLifecycle(
                        lifecycle = lifecycle,
                        maxCounter = instantTimeoutSeconds,
                        onTimeout = {
                            adClient.removeRequester(requester)
                            adResponse.adShowingFailed(AdInstantLoadTimeoutException("instant ad failed to load in specified time"))
                        }
                    )
                }
                adClient.addAdRequester(requester)
            } else {
                adResponse.adShowingFailed(AdInstantLoadNoInternetException("instant ad failed because of no internet"))
            }
        } else {
            adClient.addAdRequester(requester)
        }
    }

    companion object {

        private const val TAG = "FullScreenAdShowManager"
    }
}
