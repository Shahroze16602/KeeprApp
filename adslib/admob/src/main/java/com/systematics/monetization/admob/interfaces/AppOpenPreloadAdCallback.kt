package com.systematics.monetization.admob.interfaces

import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd

interface AppOpenPreloadAdCallback : PreloadAdCallback {

    fun onAdLoaded(appOpenAd: AppOpenAd)
}