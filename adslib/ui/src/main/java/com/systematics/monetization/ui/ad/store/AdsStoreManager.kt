package com.systematics.monetization.ui.ad.store

import android.util.Log

interface AdsStoreManager {


    val nativeAdStore: NativeAdStore
    val bannerAdStore: BannerAdStore


    fun isPreserved(placementKey: String): Boolean {
        return nativeAdStore.isPreserved(placementKey) || bannerAdStore.isPreserved(placementKey)
    }

    fun clearStore() {
        Log.d(TAG, "clearStore: ")
        nativeAdStore.clear()
        bannerAdStore.clear()
    }

    companion object {

        private const val TAG = "AdsViewModelTAG"
    }
}