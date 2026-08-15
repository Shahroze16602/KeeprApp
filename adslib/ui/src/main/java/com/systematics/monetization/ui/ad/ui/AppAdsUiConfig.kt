package com.systematics.monetization.ui.ad.ui

import com.systematics.monetization.ui.placement.models.FullScreenPlacementModel
import com.systematics.monetization.ui.placement.models.abs.InlinePlacementModel

data class AppAdsUiConfig(
    val appFullScreens: Map<String, FullScreenPlacementModel>,
    val appInlines: Map<String, InlinePlacementModel>,
)