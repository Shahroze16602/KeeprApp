package com.systematics.app.utils.monetization.config.frontend.breakpoints.local

import com.systematics.monetization.ui.breakpoints.AdGroupBreakPointModel
import com.systematics.monetization.ui.breakpoints.BreakPointModel
import com.systematics.app.utils.monetization.config.backend.AdGroupType
import com.systematics.app.utils.monetization.config.frontend.breakpoints.AdBreakPoint
import com.systematics.app.utils.monetization.config.frontend.config.AdsPlacement

val appBreakPointConfig = mapOf(
    AdBreakPoint.BP_PREMIUM_PENDING to BreakPointModel(
        adgroupBreakPoints = listOf()
    ),
    AdBreakPoint.BP_LANGUAGE_PENDING to BreakPointModel(
        adgroupBreakPoints = listOf(
            AdGroupBreakPointModel(
                adGroupType = AdGroupType.Native.LANGUAGE,
                placementKeys = listOf(AdsPlacement.Inlines.LANGUAGE_BOTTOM)
            )
        )
    ),
    AdBreakPoint.BP_ONBOARDING_PENDING to BreakPointModel(
        adgroupBreakPoints = listOf(
            AdGroupBreakPointModel(
                adGroupType = AdGroupType.Native.ONBOARDING,
                placementKeys = listOf(AdsPlacement.Inlines.ONBOARDING_BOTTOM)
            )
        )
    ),
    AdBreakPoint.BP_HOME_PENDING to BreakPointModel(
        adgroupBreakPoints = listOf(
            AdGroupBreakPointModel(
                adGroupType = AdGroupType.Native.HOME,
                placementKeys = listOf(AdsPlacement.Inlines.HOME_BOTTOM)
            )
        )
    ),
    AdBreakPoint.BP_HOME_APPEAR to BreakPointModel(
        adgroupBreakPoints = listOf(
            AdGroupBreakPointModel(
                adGroupType = AdGroupType.FullScreenAd.APP_OPEN,
                placementKeys = listOf(AdsPlacement.FullScreens.APP_OPEN_SWITCH)
            ),
            AdGroupBreakPointModel(
                adGroupType = AdGroupType.FullScreenAd.COMMON_INTER,
                placementKeys = listOf()
            ),
            AdGroupBreakPointModel(
                adGroupType = AdGroupType.FullScreenAd.COMMON_INTER,
                placementKeys = listOf()
            ),
        )
    ),
)