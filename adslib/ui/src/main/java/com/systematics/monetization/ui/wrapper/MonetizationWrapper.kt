package com.systematics.monetization.ui.wrapper

import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.NativeAdOneTimeUiEvents
import com.systematics.monetization.ui.ad.FullScreenAdShowManager
import com.systematics.monetization.ui.ad.store.AdsStoreManager
import com.systematics.monetization.ui.exceptions.AdPlacementNotAllowedException
import com.systematics.monetization.ui.exceptions.AdPlacementNotFoundException
import com.systematics.monetization.ui.models.PlacementUiModel
import com.systematics.monetization.ui.placement.models.BannerPlacementModel
import com.systematics.monetization.ui.placement.models.FullScreenPlacementModel
import com.systematics.monetization.ui.placement.models.NativePlacementModel
import com.systematics.monetization.ui.remote.MonetizationRemote
import com.systematics.monetization.ui.utils.monetizationGet
import com.systematics.monetization.ui.utils.monetizationInject
import com.systematics.monetization.ui.viewmodel.AdsViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MonetizationWrapper(
    private val activity: ComponentActivity,
    private val lifecycle: Lifecycle = activity.lifecycle,
    val adsViewModel: AdsStoreManager = ViewModelProvider(activity)[AdsViewModel::class.java]
) {

    private val monetizationApp: MonetizationApp by monetizationInject()

    private val fullScreenAdShowManager: FullScreenAdShowManager by monetizationInject()

    private val monetizationRemote: MonetizationRemote by monetizationInject()

    private val adsCoreRemoteConfigs: AdsCoreRemoteConfigs by monetizationInject()
    private val nativeShowWrapper: NativeShowWrapper by lazy {
        NativeShowWrapper(
            context = activity,
            lifecycle = lifecycle,
            adsCoreRemoteConfigs = adsCoreRemoteConfigs,
            nativeLoadManager = monetizationGet(),
        )
    }

    private val bannerShowWrapper: BannerShowWrapper by lazy {
        BannerShowWrapper(
            activity = activity,
            lifecycle = lifecycle,
            adsCoreRemoteConfigs = adsCoreRemoteConfigs
        )
    }

    fun showFullScreenAd(
        placementKey: String,
        onPlacementData: (FullScreenPlacementModel) -> Unit = {},
        onAdLoaded: () -> Unit = {},
        onAdShowing: () -> Unit = {},
        onDone: (Boolean) -> Unit = {},
    ) {
        showFullScreenAd(
            placementKey = placementKey,
            onPlacementData = onPlacementData,
            onAdLoaded = onAdLoaded,
            onAdShowing = onAdShowing,
            onAdShown = { onDone(true) },
            onFailed = { onDone(false) }
        )
    }

    fun showFullScreenAd(
        placementKey: String,
        onPlacementData: (FullScreenPlacementModel) -> Unit = {},
        onAdLoaded: () -> Unit = {},
        onAdShowing: () -> Unit = {},
        onAdShown: () -> Unit = {},
        onFailed: (Exception) -> Unit = {}
    ) {
        fullScreenAdShowManager.showFullScreenAd(
            lifecycle = lifecycle,
            coroutineScope = lifecycle.coroutineScope,
            activity = activity,
            placementKey = placementKey,
            onPlacementData = onPlacementData,
            onAdLoaded = onAdLoaded,
            onAdShowing = onAdShowing,
            onAdShown = onAdShown,
            onFailed = onFailed,
        )
    }

    fun showInlineAd(
        placementKey: String,
        adview: ViewGroup,
        backupPlacementKey: String = placementKey + "_Backup",
        nativeUiEvents: Flow<NativeAdOneTimeUiEvents>? = null,
        onAdShown: (PlacementUiModel) -> Unit = {},
        onFailed: (Exception) -> Unit = {}
    ) {
        if (adsCoreRemoteConfigs.monetizationEnabled()) {
            if (monetizationApp.isInitialized.value) {
                showInlineAdNow(
                    adsStoreManager = adsViewModel,
                    placementKey = placementKey,
                    adview = adview,
                    backupPlacementKey = backupPlacementKey,
                    nativeUiEvents = nativeUiEvents,
                    onAdShown = onAdShown,
                    onFailed = onFailed
                )
            } else {
                lifecycle.coroutineScope.launch {
                    lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                        monetizationApp.isInitialized
                            .filter { it }
                            .first()
                            .also {
                                showInlineAdNow(
                                    adsStoreManager = adsViewModel,
                                    placementKey = placementKey,
                                    adview = adview,
                                    backupPlacementKey = backupPlacementKey,
                                    nativeUiEvents = nativeUiEvents,
                                    onAdShown = onAdShown,
                                    onFailed = onFailed
                                )
                            }
                    }
                }
            }
        }
    }

    private fun showInlineAdNow(
        adsStoreManager: AdsStoreManager,
        placementKey: String,
        adview: ViewGroup,
        backupPlacementKey: String = placementKey,
        nativeUiEvents: Flow<NativeAdOneTimeUiEvents>?,
        onAdShown: (PlacementUiModel) -> Unit = {},
        onFailed: (Exception) -> Unit = {}
    ) {
        lifecycle.coroutineScope.launch {
            adsCoreRemoteConfigs.monetizationEnabledState.collect {
                if (!it) {
                    Log.d(TAG, "showInlineAd: monetization disabled in between")
                    adview.removeAllViews()
                }
            }
        }
        val getPlacementUiModel: (String) -> PlacementUiModel? = { key ->
            if (adsCoreRemoteConfigs.isPlacementOffForcefully(key)) {
                Log.d(TAG, "showInlineAdNow: forcefully off for placement $key")
                onFailed(AdPlacementNotAllowedException("Placement forcefully off exception"))
                null
            } else {
                val placement = monetizationRemote.appAdsUiConfig.appInlines[key]
                if (placement == null) {
                    ALog.d(TAG, "No such placement for $key")
                    onFailed(AdPlacementNotFoundException("No such placement for $key"))
                    null
                } else {
                    if (!placement.isEnabled) {
                        ALog.d(TAG, "$key placement not allowed")
                        onFailed(AdPlacementNotAllowedException("${placement.tag} placement not allowed No placement allowed"))
                        null
                    } else {
                        Log.d(TAG, "showInlineAdNow: setting current placement $placement")
                        PlacementUiModel(
                            key = key,
                            placement = placement
                        )
                    }
                }
            }
        }
        val onFailureInternal: (Exception, PlacementUiModel) -> Unit = { ex, failedUiPlacement ->
            Log.d(TAG, "placement ${failedUiPlacement.key} failed $ex")
            val isMainPlacement = placementKey == failedUiPlacement.key
            if (isMainPlacement) {
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    val backupPlacement =
                        monetizationRemote.getInlinePlacementModel(backupPlacementKey)
                    if (backupPlacement != null) {
                        ALog.d(
                            TAG,
                            "${failedUiPlacement.key} shifting to backup placement $backupPlacementKey"
                        )
                        val backupPlacementModel = getPlacementUiModel(backupPlacementKey)
                        backupPlacementModel?.let { uiPlacement ->
                            showPlacementAd(
                                adsViewModel = adsStoreManager,
                                placementKey = uiPlacement.key,
                                uiPlacement = uiPlacement,
                                adview = adview,
                                nativeUiEvents = nativeUiEvents,
                                onAdShown = { onAdShown(uiPlacement) },
                                onFailed = onFailed
                            )
                        }
                    } else {
                        onFailed(ex)
                    }
                } else {
                    onFailed(ex)
                }
            } else {
                onFailed(ex)
            }
        }

        val placementModel = if (adsStoreManager.isPreserved(backupPlacementKey)) {
            getPlacementUiModel(backupPlacementKey)
        } else {
            getPlacementUiModel(placementKey)
        }

        placementModel?.let { uiPlacement ->
            showPlacementAd(
                adsViewModel = adsStoreManager,
                placementKey = uiPlacement.key,
                uiPlacement = uiPlacement,
                adview = adview,
                nativeUiEvents = nativeUiEvents,
                onAdShown = { onAdShown(uiPlacement) },
                onFailed = { onFailureInternal(it, uiPlacement) }
            )
        }
    }

    private fun showPlacementAd(
        adsViewModel: AdsStoreManager,
        placementKey: String,
        uiPlacement: PlacementUiModel,
        adview: ViewGroup,
        nativeUiEvents: Flow<NativeAdOneTimeUiEvents>?,
        onAdShown: (PlacementUiModel) -> Unit = {},
        onFailed: (Exception) -> Unit = {}
    ) {
        val nativeThemedUiModel =
            monetizationRemote.getNativeUiConfigForPlacement(placementKey)
        when (val placementModel = uiPlacement.placement) {
            is BannerPlacementModel -> {
                bannerShowWrapper.showBannerAd(
                    adView = adview,
                    placementKey = placementKey,
                    bannerPlacementModel = placementModel,
                    bannerAdStore = adsViewModel.bannerAdStore,
                    nativeThemedUiModel = nativeThemedUiModel,
                    onBannerAdShown = { onAdShown(uiPlacement) },
                    onAdFailed = onFailed,
                )
            }

            is NativePlacementModel -> {
                nativeShowWrapper.showNativeAd(
                    adView = adview,
                    placementKey = placementKey,
                    nativePlacementModel = placementModel,
                    nativeAdStore = adsViewModel.nativeAdStore,
                    nativeThemedUiModel = nativeThemedUiModel,
                    nativeUiEvents = nativeUiEvents,
                    onNativeAdShow = { onAdShown(uiPlacement) },
                    onAdFailed = onFailed
                )
            }
        }
    }

    companion object {

        private const val TAG = "MonetizationWrapperTAG"
    }
}