package com.systematics.monetization.core.models.banner

import android.view.View

abstract class AppBannerAdView<T : View>(
    val view: T
) {

    abstract fun destroy()
}