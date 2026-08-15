package com.systematics.monetization.admob.models.banners

import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.systematics.monetization.core.models.banner.AppBannerAdView

class AdmobAppBannerAdView(adView: AdView) : AppBannerAdView<AdView>(adView) {

    override fun destroy() {
        view.destroy()
    }
}