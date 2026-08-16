package com.systematics.photocleaner.swipedelete.utils.monetization.config.backend.local

import com.systematics.monetization.admob.utils.AdmobAdType
import com.systematics.monetization.core.models.ad.remote.RemoteAdInfo
import com.systematics.monetization.core.models.ad.remote.group.RotationFallbackRemoteAdInfoGroup
import com.systematics.photocleaner.swipedelete.utils.monetization.config.backend.AdGroupType

// Replace with the production native AdMob unit ID.
private const val ADMOB_NATIVE_AD_UNIT_ID = ""

val appNatives = mapOf(
    AdGroupType.Native.LANGUAGE to RotationFallbackRemoteAdInfoGroup(
        rotatedAds = listOf(
            RemoteAdInfo(
                adUnitId = ADMOB_NATIVE_AD_UNIT_ID,
                adType = AdmobAdType.NATIVE,
                adTAG = "Native Language",
                matchedTAG = "matched_native_language"
            )
        ),
        fallbackAds = listOf(),
        adType = AdGroupType.Native.LANGUAGE,
        adTAG = "Language",
        singletonAd = true,
        repeat = false
    ),
    AdGroupType.Native.ONBOARDING to RotationFallbackRemoteAdInfoGroup(
        rotatedAds = listOf(
            RemoteAdInfo(
                adUnitId = ADMOB_NATIVE_AD_UNIT_ID,
                adType = AdmobAdType.NATIVE,
                adTAG = "Native Onboarding",
                matchedTAG = "matched_native_onboarding"
            )
        ),
        fallbackAds = listOf(),
        adType = AdGroupType.Native.ONBOARDING,
        adTAG = "Onboarding",
        repeat = false
    ),
    AdGroupType.Native.HOME to RotationFallbackRemoteAdInfoGroup(
        rotatedAds = listOf(
            RemoteAdInfo(
                adUnitId = ADMOB_NATIVE_AD_UNIT_ID,
                adType = AdmobAdType.NATIVE,
                adTAG = "Native Home",
                matchedTAG = "matched_native_home"
            )
        ),
        fallbackAds = listOf(),
        adType = AdGroupType.Native.HOME,
        adTAG = "Home",
        repeat = false
    ),
    AdGroupType.Native.COMMON to RotationFallbackRemoteAdInfoGroup(
        rotatedAds = listOf(
            RemoteAdInfo(
                adUnitId = ADMOB_NATIVE_AD_UNIT_ID,
                adType = AdmobAdType.NATIVE,
                adTAG = "Native Common",
                matchedTAG = "matched_native_common"
            )
        ),
        fallbackAds = listOf(),
        adType = AdGroupType.Native.COMMON,
        adTAG = "Common"
    )
)
