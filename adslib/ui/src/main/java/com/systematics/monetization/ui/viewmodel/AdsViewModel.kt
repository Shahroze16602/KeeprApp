package com.systematics.monetization.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.systematics.monetization.ui.ad.store.AdsStoreManager
import com.systematics.monetization.ui.ad.store.BannerAdStore
import com.systematics.monetization.ui.ad.store.NativeAdStore

class AdsViewModel : ViewModel(), AdsStoreManager {

    override val nativeAdStore = NativeAdStore()
    override val bannerAdStore = BannerAdStore()

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: ")
        clearStore()
    }

    companion object {

        private const val TAG = "AdsViewModelTAG"
    }
}