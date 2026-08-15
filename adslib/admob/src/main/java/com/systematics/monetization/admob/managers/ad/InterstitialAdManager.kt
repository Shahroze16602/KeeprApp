package com.systematics.monetization.admob.managers.ad

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import com.systematics.monetization.admob.AdmobNetworkApp
import com.systematics.monetization.admob.revenue.models.AdmobRevenue
import com.systematics.monetization.admob.utils.AdmobAdType
import com.systematics.monetization.core.interfaces.AdShowListener
import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.models.AdLoadState
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.EventsConstants.AD_NOT_LOADED
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class InterstitialAdManager(
    context: Context,
    adInfo: AdInfo
) : AdManager<InterstitialAd>(context, adInfo) {

    private val adRequest: AdRequest = AdRequest.Builder(adInfo.adUnitId).build()
    private var mInterstitialAd: InterstitialAd? = null

    override fun adLoadingPermitted() = AdmobNetworkApp.instance.adsLoadingPermitted(adInfo.adType)

    override fun load(loadListenerInternal: AdLoadListener?) {
        if (!adLoadingPermitted()) {
            ALog.d(TAG, "${adInfo.adTAG} loading not permitted")
            val exception = Exception("Ad loading not permitted")
            loadState = AdLoadState.Failed(exception)
            loadListenerInternal?.onAdFailed(exception)
            adLoadListener?.onAdFailed(exception)
            return
        }
        loadState = AdLoadState.Loading
        ALog.d(TAG, "${adInfo.adTAG} loading")
        InterstitialAd.load(
            adRequest,
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    ALog.d(TAG, "${adInfo.adTAG} failed to load ${adError.message}")
                    mInterstitialAd = null
                    val exception = Exception(adError.message)
                    loadState = AdLoadState.Failed(exception)
                    loadListenerInternal?.onAdFailed(exception)
                    adLoadListener?.onAdFailed(exception)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    ALog.d(TAG, "${adInfo.adTAG} loaded")
                    mInterstitialAd = ad
                    loadState = AdLoadState.Loaded(this@InterstitialAdManager)
                    loadListenerInternal?.onAdLoaded()
                    adLoadListener?.onAdLoaded()
                }
            })
    }

    override fun show(activity: Activity, adShowListener: AdShowListener) {
        mInterstitialAd?.let {
            ALog.d(TAG, "${adInfo.adTAG} showing")
            it.adEventCallback = object : InterstitialAdEventCallback {

                override fun onAdDismissedFullScreenContent() {
                    MainScope().launch {
                        ALog.d(TAG, "${adInfo.adTAG} shown")
                        loadState = AdLoadState.Shown
                        adShowListener.onAdShown()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(
                    fullScreenContentError: FullScreenContentError
                ) {
                    MainScope().launch {
                        ALog.d(
                            TAG,
                            "${adInfo.adTAG} failed to show ${fullScreenContentError.message}"
                        )
                        adShowListener.adShowingFailed(Exception(fullScreenContentError.message))
                    }
                }

                override fun onAdPaid(value: AdValue) {
                    val extraMap: MutableMap<String, Any> = mutableMapOf()
                    Log.d(TAG, "show: ad revenue ${AdmobNetworkApp.instance.revenueListener}")
                    AdmobNetworkApp.instance.revenueListener.onRevenue(
                        revenueModel = AdmobRevenue(
                            adValue = value,
                            extras = extraMap,
                            adUnitId = adInfo.adUnitId,
                            adType = AdmobAdType.INTERSTITIAL
                        )
                    )
                }
            }
            it.show(activity)

        } ?: run {
            Log.d(TAG, "show: ad not loaded yet to show")
            adShowListener.adShowingFailed(Exception(AD_NOT_LOADED))
        }
    }

    override fun destroy() {
        mInterstitialAd = null
        loadState = AdLoadState.Destroyed
    }

    override fun getLoadedAd() = mInterstitialAd


    companion object {

        private const val TAG = "Admob Interstitial"
    }
}