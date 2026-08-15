package com.systematics.app.presentation.screens.feature

import androidx.compose.runtime.Composable
import com.systematics.app.utils.monetization.config.frontend.config.AdsPlacement

@Composable
fun FeatureTwoScreen(
    onBack: () -> Unit
) {
    FeatureScreen(
        featureName = "Feature Two",
        bottomPlacement = AdsPlacement.Inlines.FEATURE_TWO_BOTTOM,
        backPlacement = AdsPlacement.FullScreens.FEATURE_TWO_BACK,
        actionPlacement = AdsPlacement.FullScreens.FEATURE_TWO_ACTION,
        onBack = onBack
    )
}
