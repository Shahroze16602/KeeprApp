package com.systematics.monetization.ui.wrapper

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.repeatOnLifecycle
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.models.banner.AppBannerAdView
import com.systematics.monetization.core.models.banner.BannerAdInfo
import com.systematics.monetization.core.models.natives.NativeThemedUiDataModel
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.dpToPx
import com.systematics.monetization.core.utils.toColor
import com.systematics.monetization.ui.ad.store.BannerAdStore
import com.systematics.monetization.ui.placement.models.BannerPlacementModel
import com.systematics.monetization.ui.utils.startShimmerColor
import com.systematics.monetization.ui.utils.stopShimmerColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BannerShowWrapper(
    private val activity: Activity,
    private val lifecycle: Lifecycle,
    private val adsCoreRemoteConfigs: AdsCoreRemoteConfigs
) {

    private var bannerLoaded = false
    private var shimmerHeight = 0

    fun showBannerAd(
        adView: ViewGroup,
        placementKey: String,
        bannerPlacementModel: BannerPlacementModel,
        bannerAdStore: BannerAdStore,
        nativeThemedUiModel: NativeThemedUiDataModel,
        onBannerAdShown: () -> Unit = {},
        onAdFailed: (Exception) -> Unit = {}
    ) {
        val nativeUiDataModel = nativeThemedUiModel.themed(activity)
        lifecycle.coroutineScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (!adsCoreRemoteConfigs.monetizationEnabled()) {
                    Log.d(TAG, "showBannerAd: monetization disabled")
                    adView.removeAllViews()
                    return@repeatOnLifecycle
                }
                showBannerAd(
                    adView = adView,
                    placementKey = placementKey,
                    placement = bannerPlacementModel,
                    bannerAdStore = bannerAdStore,
                    bannerAdInfo = bannerPlacementModel.bannerAdInfo,
                    adTag = bannerPlacementModel.tag,
                    shimmerColor = nativeUiDataModel.shimmerColor.toColor(),
                    onBannerAdShown = onBannerAdShown,
                    onAdFailed = onAdFailed
                )
            }
        }
    }

    suspend fun <T : BannerAdInfo> showBannerAd(
        adView: ViewGroup,
        placementKey: String,
        placement: BannerPlacementModel,
        bannerAdStore: BannerAdStore,
        bannerAdInfo: T,
        adTag: String,
        shimmerColor: Int,
        onBannerAdShown: () -> Unit = {},
        onAdFailed: (Exception) -> Unit = {}
    ) {
        val bannerView: AppBannerAdView<*>? = try {
            val preservedAd = bannerAdStore.getPreservedAd(placementKey)
            if (preservedAd != null) {
                Log.d(TAG, "getLoadedAd: preserved ad for $placementKey used")
                bannerLoaded = true
                preservedAd
            } else {
                val populateManager =
                    MonetizationApp.instance.integrationManager.getBannerPopulateManager<T>(
                        bannerAdInfo
                    )
                populateManager?.let { manager ->
                    shimmerHeight = manager.getShimmerHeight(activity, bannerAdInfo)
                    val bannerView = manager.populateBannerAdView(
                        activity = activity,
                        bannerAdInfo = bannerAdInfo,
                        onDone = {
                            bannerLoaded = true
                            adView.stopShimmerColor()
                            ALog.d(TAG, "$adTag banner shown")
                            onBannerAdShown()
                        },
                        onFailed = {
                            adView.stopShimmerColor()
                            ALog.e(TAG, "$adTag banner failed", it)
                            onAdFailed(it)
                        }
                    )

                    if (placement.preserved) {
                        Log.d(TAG, "getLoadedAd: preserved $placementKey")
                        bannerAdStore.preserveAd(placementKey, bannerView)
                    }
                    bannerView
                } ?: run {
                    ALog.d(
                        TAG,
                        "BannerAdShowComponent: No banner populate manager for $placement"
                    )
                    null
                }
            }
        } catch (ex: Exception) {
            ALog.d(TAG, "$adTag banner failed ${ex.message}")
            Log.e(TAG, "BannerAdShowComponent: ", ex)
            onAdFailed(ex)
            null
        }
        bannerView?.let {
            (it.view.parent as? ViewGroup)?.removeView(it.view)
            adView.removeAllViews()
            if (shimmerHeight != 0) {
                adView.layoutParams = adView.layoutParams.apply {
                    height = shimmerHeight.dpToPx.roundToInt()
                }
            }
            adView.addView(it.view)
            if (!bannerLoaded) {
                adView.startShimmerColor(shimmerColor)
            }
        }
    }


    companion object {

        private const val TAG = "BannerShowWrapperTAG"
    }
}