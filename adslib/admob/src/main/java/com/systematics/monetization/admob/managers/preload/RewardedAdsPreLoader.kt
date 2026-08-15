package com.systematics.monetization.admob.managers.preload

import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.common.PreloadCallback
import com.google.android.libraries.ads.mobile.sdk.common.PreloadConfiguration
import com.google.android.libraries.ads.mobile.sdk.common.ResponseInfo
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdPreloader
import com.systematics.monetization.admob.managers.core.AdmobPreLoader

class RewardedAdsPreLoader(
    private val adUnitId: String,
    private val isAdNeeded: (String) -> Boolean,
    private val onAdPreloaded: (String, RewardedAd) -> Unit,
    private val onAdFailed: (String, LoadAdError) -> Unit
) : AdmobPreLoader<RewardedAd>() {

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
        RewardedAdPreloader.start(adUnitId, configuration, callback)
    }

    override fun getAd(): RewardedAd? {
        return RewardedAdPreloader.pollAd(adUnitId)
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
        return RewardedAdPreloader.isAdAvailable(adUnitId)
    }

    companion object {

        private const val TAG = "RewardedAdsPreLoaderTAG"
    }

}