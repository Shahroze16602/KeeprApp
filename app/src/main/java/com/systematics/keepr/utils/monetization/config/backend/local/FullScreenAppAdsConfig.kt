package com.systematics.keepr.utils.monetization.config.backend.local

import com.systematics.monetization.admob.utils.AdmobAdType
import com.systematics.monetization.core.models.ad.remote.RemoteAdInfo
import com.systematics.monetization.core.models.ad.remote.group.RotationFallbackRemoteAdInfoGroup
import com.systematics.keepr.utils.monetization.config.backend.AdGroupType

// Replace with the production full-screen AdMob unit IDs.
private const val ADMOB_INTERSTITIAL_AD_UNIT_ID = ""
private const val ADMOB_APP_OPEN_AD_UNIT_ID = ""

val appFullScreens = mapOf(
    AdGroupType.FullScreenAd.SPLASH_AD to RotationFallbackRemoteAdInfoGroup(
        rotatedAds = listOf(
            RemoteAdInfo(
                adUnitId = ADMOB_INTERSTITIAL_AD_UNIT_ID,
                adType = AdmobAdType.INTERSTITIAL,
                adTAG = "Splash Inter",
                matchedTAG = "matched_inter_splash"
            )
        ),
        fallbackAds = listOf(),
        adType = AdGroupType.FullScreenAd.SPLASH_AD,
        adTAG = "Splash Ad",
        singletonAd = true,
        repeat = false
    ),
    AdGroupType.FullScreenAd.COMMON_INTER to RotationFallbackRemoteAdInfoGroup(
        rotatedAds = listOf(
            RemoteAdInfo(
                adUnitId = ADMOB_INTERSTITIAL_AD_UNIT_ID,
                adType = AdmobAdType.INTERSTITIAL,
                adTAG = "Inter Common",
                matchedTAG = "matched_inter_common"
            )
        ),
        fallbackAds = listOf(),
        adType = AdGroupType.FullScreenAd.COMMON_INTER,
        adTAG = "Common Ad"
    ),
    AdGroupType.FullScreenAd.APP_OPEN to RotationFallbackRemoteAdInfoGroup(
        rotatedAds = listOf(
            RemoteAdInfo(
                adUnitId = ADMOB_APP_OPEN_AD_UNIT_ID,
                adType = AdmobAdType.APP_OPEN,
                adTAG = "App Open",
                matchedTAG = "matched_app_open"
            )
        ),
        fallbackAds = arrayListOf(),
        adType = AdGroupType.FullScreenAd.APP_OPEN,
        adTAG = "App Open"
    )
)
