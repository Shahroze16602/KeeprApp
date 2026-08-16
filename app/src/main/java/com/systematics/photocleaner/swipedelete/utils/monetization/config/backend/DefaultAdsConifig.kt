package com.systematics.photocleaner.swipedelete.utils.monetization.config.backend

import com.systematics.photocleaner.swipedelete.utils.monetization.config.backend.local.appFullScreens
import com.systematics.photocleaner.swipedelete.utils.monetization.config.backend.local.appNatives
import com.systematics.monetization.core.models.AppAdsConfig

val defaultAppAdsConfig = AppAdsConfig(
    appFullScreens = appFullScreens,
    appNatives = appNatives
)
