package com.systematics.app.utils.monetization.config.backend

import com.systematics.app.utils.monetization.config.backend.local.appFullScreens
import com.systematics.app.utils.monetization.config.backend.local.appNatives
import com.systematics.monetization.core.models.AppAdsConfig

val defaultAppAdsConfig = AppAdsConfig(
    appFullScreens = appFullScreens,
    appNatives = appNatives
)