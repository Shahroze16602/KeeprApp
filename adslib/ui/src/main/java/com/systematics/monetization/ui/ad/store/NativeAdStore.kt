package com.systematics.monetization.ui.ad.store

import android.util.Log
import com.systematics.monetization.core.interfaces.AdStore
import com.systematics.monetization.core.models.natives.AppRefreshableNativeAd

class NativeAdStore : AdStore<AppRefreshableNativeAd> {

    override val adStore: MutableMap<String, AppRefreshableNativeAd> = mutableMapOf()

    override fun clear() {
        adStore.forEach {
            Log.d(TAG, "clear: destroying ${it.key}")
            it.value.appNativeAd.destroy()
        }
    }

    companion object {

        private const val TAG = "NativeAdStoreTAG"
    }
}