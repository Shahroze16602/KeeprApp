package com.systematics.monetization.ui.compose

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.systematics.monetization.ui.ad.FullScreenAdShowManager
import com.systematics.monetization.ui.compose.models.AdAction
import com.systematics.monetization.ui.compose.utils.monetizationInject
import com.systematics.monetization.ui.placement.models.FullScreenPlacementModel

@Composable
fun FullScreenAdShowComponent(
    fullScreenAdShowManager: FullScreenAdShowManager = monetizationInject(),
    adAction: AdAction? = null,
    onPlacementData: (FullScreenPlacementModel) -> Unit = {},
    onAdLoaded: () -> Unit = {},
) {
    adAction?.let {
        FullScreenAdShowComponent(
            fullScreenAdShowManager = fullScreenAdShowManager,
            showAd = true,
            placementKey = adAction.placementKey,
            onPlacementData = onPlacementData,
            onAdLoaded = onAdLoaded,
            onDone = { adAction.action(it) }
        )
    }
}

@Composable
fun FullScreenAdShowComponent(
    fullScreenAdShowManager: FullScreenAdShowManager = monetizationInject(),
    showAd: Boolean,
    placementKey: String,
    onPlacementData: (FullScreenPlacementModel) -> Unit = {},
    onAdLoaded: () -> Unit = {},
    onDone: (Boolean) -> Unit = {}
) {
    val activity = LocalActivity.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(key1 = showAd) {
        if (showAd) {
            fullScreenAdShowManager.showFullScreenAd(
                lifecycle = lifecycle,
                coroutineScope = this,
                activity = activity!!,
                placementKey = placementKey,
                onPlacementData = onPlacementData,
                onAdLoaded = onAdLoaded,
                onDone = onDone
            )
        }
    }
}

@Composable
fun FullScreenAdShowComponent(
    fullScreenAdShowManager: FullScreenAdShowManager = monetizationInject(),
    showAd: Boolean,
    placementKey: String,
    onPlacementData: (FullScreenPlacementModel) -> Unit = {},
    onAdLoaded: () -> Unit = {},
    onAdShown: () -> Unit = {},
    onFailed: (Exception) -> Unit = {}
) {
    val activity = LocalActivity.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(key1 = showAd) {
        if (showAd) {
            fullScreenAdShowManager.showFullScreenAd(
                lifecycle = lifecycle,
                coroutineScope = this,
                activity = activity!!,
                placementKey = placementKey,
                onPlacementData = onPlacementData,
                onAdLoaded = onAdLoaded,
                onAdShown = onAdShown,
                onFailed = onFailed
            )
        }
    }
}