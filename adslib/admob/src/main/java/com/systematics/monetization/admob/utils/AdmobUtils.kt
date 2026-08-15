package com.systematics.monetization.admob.utils

import com.systematics.monetization.core.utils.AD_ID_TEST_INTERSTITIAL
import com.systematics.monetization.core.utils.AD_ID_TEST_NATIVE_AD
import com.systematics.monetization.core.utils.AD_ID_TEST_OPEN_AD
import com.systematics.monetization.core.utils.AD_ID_TEST_REWARDED
import com.systematics.monetization.core.utils.AD_ID_TEST_REWARDED_INTER

fun getDebugAdUnitForAdType(adType: String): String {
    return when (adType) {
        AdmobAdType.INTERSTITIAL, AdmobAdType.INTERSTITIAL_PRE_LOAD -> AD_ID_TEST_INTERSTITIAL
        AdmobAdType.REWARDED, AdmobAdType.REWARDED_PRE_LOAD -> AD_ID_TEST_REWARDED
        AdmobAdType.REWARDED_INTERSTITIAL -> AD_ID_TEST_REWARDED_INTER
        AdmobAdType.NATIVE -> AD_ID_TEST_NATIVE_AD
        AdmobAdType.APP_OPEN, AdmobAdType.APP_OPEN_PRE_LOAD -> AD_ID_TEST_OPEN_AD
        else -> ""
    }
}