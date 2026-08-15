package com.systematics.monetization.admob.interfaces

import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd

interface RewardedPreloadAdCallback : PreloadAdCallback {

    fun onAdLoaded(rewardedAd: RewardedAd)
}