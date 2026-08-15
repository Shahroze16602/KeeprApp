package com.systematics.monetization.admob.ui

import android.content.Context
import android.view.View
import com.systematics.monetization.admob.models.natives.AdmobAppNativeAd
import com.systematics.monetization.core.managers.populate.NativePopulateManager

class AdmobNativePopulateManager : NativePopulateManager<AdmobAppNativeAd>() {

    override fun populateNativeAd(
        context: Context,
        nativeAd: AdmobAppNativeAd,
        layout: String
    ): View {
        TODO("")
    }
}