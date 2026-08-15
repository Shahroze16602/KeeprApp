package com.systematics.monetization.admob.interfaces

import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

interface PreloadAdCallback {

    fun onAdFailedToLoad(adError: LoadAdError)

}