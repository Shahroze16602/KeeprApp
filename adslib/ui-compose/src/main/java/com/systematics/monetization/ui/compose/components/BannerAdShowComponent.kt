package com.systematics.monetization.ui.compose.components

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.models.banner.AppBannerAdView
import com.systematics.monetization.core.models.banner.BannerAdInfo
import com.systematics.monetization.core.models.natives.NativeThemedUiDataModel
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.toColor
import com.systematics.monetization.ui.ad.store.BannerAdStore
import com.systematics.monetization.ui.compose.utils.shimmerColor
import com.systematics.monetization.ui.placement.models.BannerPlacementModel

private const val TAG = "Banner AdShowComponent"

@Composable
internal fun BannerAdShowComponent(
    modifier: Modifier = Modifier,
    placementKey: String,
    bannerPlacementModel: BannerPlacementModel,
    bannerAdStore: BannerAdStore,
    nativeThemedUiModel: NativeThemedUiDataModel,
    onBannerAdShown: () -> Unit = {},
    onAdFailed: (Exception) -> Unit = {}
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()
    val nativeUiDataModel by remember(isDark) {
        mutableStateOf(nativeThemedUiModel.themed(isDark))
    }
    BannerAdShowComponent(
        modifier = modifier,
        placementKey = placementKey,
        placement = bannerPlacementModel,
        bannerAdStore = bannerAdStore,
        bannerAdInfo = bannerPlacementModel.bannerAdInfo,
        adTag = bannerPlacementModel.tag,
        shimmerColor = remember { Color(nativeUiDataModel.shimmerColor.toColor()) },
        onBannerAdShown = onBannerAdShown,
        onAdFailed = onAdFailed
    )
}

@Composable
private fun <T : BannerAdInfo> BannerAdShowComponent(
    modifier: Modifier = Modifier,
    placementKey: String,
    placement: BannerPlacementModel,
    bannerAdStore: BannerAdStore,
    bannerAdInfo: T,
    adTag: String,
    shimmerColor: Color,
    onBannerAdShown: () -> Unit = {},
    onAdFailed: (Exception) -> Unit = {}
) {
    val activity = LocalActivity.current
    var bannerView by remember {
        mutableStateOf<AppBannerAdView<*>?>(null)
    }

    var bannerLoaded by remember {
        mutableStateOf(false)
    }

    var shimmerHeight by remember {
        mutableStateOf(60.dp)
    }
    var bannerFailed by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(key1 = Unit) {
        bannerView = try {
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
                    shimmerHeight = manager.getShimmerHeight(activity!!, bannerAdInfo).dp
                    val bannerView = manager.populateBannerAdView(
                        activity = activity,
                        bannerAdInfo = bannerAdInfo,
                        onDone = {
                            bannerLoaded = true
                            ALog.d(TAG, "$adTag banner shown")
                            onBannerAdShown()
                        },
                        onFailed = {
                            bannerFailed = true
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
                    ALog.d(TAG, "BannerAdShowComponent: No banner populate manager for $placement")
                    null
                }
            }
        } catch (ex: Exception) {
            ALog.d(TAG, "$adTag banner failed ${ex.message}")
            Log.e(TAG, "BannerAdShowComponent: ", ex)
            onAdFailed(ex)
            null
        }
    }
    if (!bannerFailed) {
        bannerView?.let {
            BannerAdShowComponent(
                modifier = if (bannerLoaded) modifier else modifier
                    .fillMaxWidth()
                    .height(shimmerHeight)
                    .background(shimmerColor(shimmerColor = shimmerColor)),
                appBannerAdView = it
            )
        }
    }
}

@Composable
private fun BannerAdShowComponent(
    modifier: Modifier = Modifier,
    appBannerAdView: AppBannerAdView<*>
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        BannerAdShowComponent(
            modifier = modifier,
            bannerView = appBannerAdView.view
        )
    }
}

@Composable
private fun BannerAdShowComponent(
    modifier: Modifier = Modifier,
    bannerView: View
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            (bannerView.parent as? ViewGroup)?.removeView(bannerView)
            bannerView
        }
    )
}