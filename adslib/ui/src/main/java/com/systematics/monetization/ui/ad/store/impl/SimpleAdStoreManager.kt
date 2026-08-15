package com.systematics.monetization.ui.ad.store.impl

import android.util.Log
import com.systematics.monetization.ui.ad.store.AdsStoreManager
import com.systematics.monetization.ui.ad.store.BannerAdStore
import com.systematics.monetization.ui.ad.store.NativeAdStore

class SimpleAdStoreManager : AdsStoreManager {


    override val nativeAdStore = NativeAdStore()
    override val bannerAdStore = BannerAdStore()

    fun clear() {
        Log.d(TAG, "onCleared: ")
        clearStore()
    }

    companion object {

        private const val TAG = "SimpleAdStoreManagerTAG"
    }
}