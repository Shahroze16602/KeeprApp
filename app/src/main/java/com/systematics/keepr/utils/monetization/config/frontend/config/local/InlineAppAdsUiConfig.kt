package com.systematics.keepr.utils.monetization.config.frontend.config.local

import com.systematics.monetization.admob.models.banners.AdmobAdaptiveBannerAdInfo
import com.systematics.monetization.ui.placement.models.BannerPlacementModel
import com.systematics.monetization.ui.placement.models.NativePlacementModel
import com.systematics.keepr.utils.monetization.config.backend.AdGroupType
import com.systematics.keepr.utils.monetization.config.frontend.config.AdsPlacement

val appInlines = mapOf(
    AdsPlacement.Inlines.LANGUAGE_BOTTOM to NativePlacementModel(
        tag = "Language Native Bottom",
        adGroupType = AdGroupType.Native.LANGUAGE,
        layout = "large_native_large_cta"
    ),
    AdsPlacement.Inlines.PREMIUM_TOP to BannerPlacementModel(
        tag = "Premium Top Banner",
        bannerAdInfo = AdmobAdaptiveBannerAdInfo(
            id = "",
            tag = "Premium Adaptive Banner"
        )
    ),
    AdsPlacement.Inlines.ONBOARDING_BOTTOM to NativePlacementModel(
        tag = "Onboarding Native Bottom",
        adGroupType = AdGroupType.Native.ONBOARDING,
        layout = "small_native_large_cta"
    ),
    AdsPlacement.Inlines.HOME_BOTTOM to NativePlacementModel(
        tag = "Home Bottom",
        adGroupType = AdGroupType.Native.HOME,
        layout = "small_native_large_cta"
    ),
    AdsPlacement.Inlines.FEATURE_ONE_BOTTOM to NativePlacementModel(
        tag = "Feature One Native Bottom",
        adGroupType = AdGroupType.Native.COMMON,
        layout = "small_native_large_cta"
    ),
    AdsPlacement.Inlines.FEATURE_TWO_BOTTOM to NativePlacementModel(
        tag = "Feature Two Native Bottom",
        adGroupType = AdGroupType.Native.COMMON,
        layout = "small_native_large_cta"
    ),
)
