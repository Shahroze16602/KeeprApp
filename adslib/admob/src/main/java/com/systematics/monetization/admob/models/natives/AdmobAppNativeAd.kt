package com.systematics.monetization.admob.models.natives

import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.systematics.monetization.core.models.natives.AppNativeAd

data class AdmobAppNativeAd(
    val nativeAd: NativeAd
) : AppNativeAd() {

    override fun destroy() {
        nativeAd.destroy()
    }
}