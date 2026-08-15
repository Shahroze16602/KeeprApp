package com.systematics.monetization.admob.interfaces

import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd

interface InterPreloadAdCallback : PreloadAdCallback {

    fun onAdLoaded(interstitialAd: InterstitialAd)
}