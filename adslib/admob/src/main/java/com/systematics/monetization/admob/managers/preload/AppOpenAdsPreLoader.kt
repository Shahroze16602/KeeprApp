package com.systematics.monetization.admob.managers.preload

import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdPreloader
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.systematics.monetization.admob.managers.core.AdmobPreLoader

class AppOpenAdsPreLoader(
    private val adUnitId: String,
    private val isAdNeeded: (String) -> Boolean,
    private val onAdPreloaded: (String, AppOpenAd) -> Unit,
    private val onAdFailed: (String, LoadAdError) -> Unit
) : AdmobPreLoader<AppOpenAd>() {

    private val adRequest: AdRequest = AdRequest.Builder(adUnitId).build()

    val configuration: PreloadConfiguration = PreloadConfiguration(adRequest, 1)

    val callback = object : PreloadCallback {
        override fun onAdPreloaded(preloadId: String, responseInfo: ResponseInfo) {
            Log.d(TAG, "onAdPreloaded: $preloadId")
            if (isAdNeeded(adUnitId)) {
                pushAd()
            } else {
                Log.d(TAG, "onAdPreloaded: ad polled in cache")
            }
        }

        override fun onAdsExhausted(preloadId: String) {
            Log.d(TAG, "onAdsExhausted: ")
        }

        override fun onAdFailedToPreload(preloadId: String, adError: LoadAdError) {
            Log.d(TAG, "onAdFailedToPreload: $preloadId")
            onAdFailed(adUnitId, adError)
        }
    }

    init {

        Log.d(TAG, "starting for $adUnitId")
        AppOpenAdPreloader.start(adUnitId, configuration, callback)
    }

    override fun getAd(): AppOpenAd? {
        return AppOpenAdPreloader.pollAd(adUnitId)
    }

    override fun pushAd() {
        getAd()?.let {
            Log.d(TAG, "pushAd: pushing ad")
            onAdPreloaded(adUnitId, it)
        } ?: run {
            Log.d(TAG, "pushAd: no ad for pushing")
            onAdFailed(adUnitId, LoadAdError(LoadAdError.ErrorCode.NOT_FOUND, "Ad not available"))
        }
    }

    override fun isAdAvailable(): Boolean {
        return AppOpenAdPreloader.isAdAvailable(adUnitId)
    }

    companion object {

        private const val TAG = "AppOpenAdsPreLoaderTAG"
    }

}