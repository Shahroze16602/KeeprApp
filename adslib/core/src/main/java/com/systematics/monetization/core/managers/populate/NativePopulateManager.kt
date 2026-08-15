package com.systematics.monetization.core.managers.populate

import android.content.Context
import android.view.View
import com.systematics.monetization.core.models.natives.AppNativeAd

abstract class NativePopulateManager<T : AppNativeAd> {

    abstract fun populateNativeAd(
        context: Context,
        nativeAd: T,
        layout: String,
    ): View
}