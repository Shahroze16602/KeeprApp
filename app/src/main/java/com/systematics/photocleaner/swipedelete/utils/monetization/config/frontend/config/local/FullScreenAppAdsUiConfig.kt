package com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.config.local

import com.systematics.monetization.ui.placement.models.FullScreenPlacementModel
import com.systematics.photocleaner.swipedelete.utils.monetization.config.backend.AdGroupType
import com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.config.AdsPlacement

val appFullScreens = mapOf(
    AdsPlacement.FullScreens.SPLASH_WELCOME_AD to FullScreenPlacementModel(
        tag = "Splash Welcome Ad",
        instantTimeoutSeconds = 15,
        showLoadingDialog = false,
        showInstantLoadingDialog = false,
        adGroupType = AdGroupType.FullScreenAd.SPLASH_AD
    ),
    AdsPlacement.FullScreens.SPLASH_ON_PREMIUM_CLOSE to FullScreenPlacementModel(
        tag = "Splash Ad On Premium Close",
        showLoadingDialog = false,
        adGroupType = AdGroupType.FullScreenAd.SPLASH_AD
    ),
    AdsPlacement.FullScreens.APP_OPEN_SWITCH to FullScreenPlacementModel(
        tag = "App Open Ad",
        adGroupType = AdGroupType.FullScreenAd.APP_OPEN,
        showLoadingDialog = false
    ),
    AdsPlacement.FullScreens.FEATURE_ONE_BACK to FullScreenPlacementModel(
        tag = "Feature One Back Ad",
        showLoadingDialog = false,
        adGroupType = AdGroupType.FullScreenAd.COMMON_INTER,
        consumableAdGroups = listOf(AdGroupType.FullScreenAd.SPLASH_AD)
    ),
    AdsPlacement.FullScreens.FEATURE_TWO_BACK to FullScreenPlacementModel(
        tag = "Feature Two Back Ad",
        showLoadingDialog = false,
        adGroupType = AdGroupType.FullScreenAd.COMMON_INTER,
        consumableAdGroups = listOf(AdGroupType.FullScreenAd.SPLASH_AD)
    ),
    AdsPlacement.FullScreens.FEATURE_ONE_ACTION to FullScreenPlacementModel(
        tag = "Feature One Action Ad",
        showLoadingDialog = false,
        adGroupType = AdGroupType.FullScreenAd.COMMON_INTER,
        consumableAdGroups = listOf(AdGroupType.FullScreenAd.SPLASH_AD)
    ),
    AdsPlacement.FullScreens.FEATURE_TWO_ACTION to FullScreenPlacementModel(
        tag = "Feature Two Action Ad",
        showLoadingDialog = false,
        adGroupType = AdGroupType.FullScreenAd.COMMON_INTER,
        consumableAdGroups = listOf(AdGroupType.FullScreenAd.SPLASH_AD)
    )
)
