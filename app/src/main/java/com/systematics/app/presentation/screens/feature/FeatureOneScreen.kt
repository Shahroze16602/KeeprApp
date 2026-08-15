package com.systematics.app.presentation.screens.feature

import androidx.compose.runtime.Composable
import com.systematics.app.utils.monetization.config.frontend.config.AdsPlacement

@Composable
fun FeatureOneScreen(
    onBack: () -> Unit
) {
    FeatureScreen(
        featureName = "Feature One",
        bottomPlacement = AdsPlacement.Inlines.FEATURE_ONE_BOTTOM,
        backPlacement = AdsPlacement.FullScreens.FEATURE_ONE_BACK,
        actionPlacement = AdsPlacement.FullScreens.FEATURE_ONE_ACTION,
        onBack = onBack
    )
}
