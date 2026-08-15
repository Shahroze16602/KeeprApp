package com.systematics.monetization.admob.utils

object AdmobAdType {

    const val INTERSTITIAL = "INTERSTITIAL"
    const val INTERSTITIAL_PRE_LOAD = "INTERSTITIAL_PRE_LOAD"
    const val REWARDED = "REWARDED"
    const val REWARDED_PRE_LOAD = "REWARDED_PRE_LOAD"
    const val REWARDED_INTERSTITIAL = "REWARDED_INTERSTITIAL"
    const val NATIVE = "NATIVE"
    const val APP_OPEN = "APP_OPEN"
    const val APP_OPEN_PRE_LOAD = "APP_OPEN_PRE_LOAD"


    val all = arrayListOf(
        INTERSTITIAL,
        INTERSTITIAL_PRE_LOAD,
        REWARDED,
        REWARDED_PRE_LOAD,
        REWARDED_INTERSTITIAL,
        NATIVE,
        APP_OPEN,
        APP_OPEN_PRE_LOAD
    )
}
