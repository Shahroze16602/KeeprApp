package com.systematics.monetization.ui.ad.store

import android.util.Log
import com.systematics.monetization.core.interfaces.AdStore
import com.systematics.monetization.core.models.banner.AppBannerAdView

class BannerAdStore : AdStore<AppBannerAdView<*>> {

    override val adStore: MutableMap<String, AppBannerAdView<*>> = mutableMapOf()

    override fun clear() {
        adStore.forEach {
            Log.d(TAG, "clear: destroying ${it.key}")
            it.value.destroy()
        }
    }

    companion object {

        private const val TAG = "BannerAdStoreTAG"
    }
}