package com.systematics.monetization.admob.managers.ad

import android.app.Activity
import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.systematics.monetization.admob.AdmobNetworkApp
import com.systematics.monetization.admob.models.natives.AdmobAppNativeAd
import com.systematics.monetization.admob.revenue.models.AdmobRevenue
import com.systematics.monetization.admob.utils.AdmobAdType
import com.systematics.monetization.core.interfaces.AdShowListener
import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.models.AdLoadState
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.utils.ALog

class NativeAdManager(
    context: Context,
    adInfo: AdInfo
) : AdManager<AdmobAppNativeAd>(context, adInfo) {

    private val mNativeAds: MutableList<NativeAd> = arrayListOf()

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
        val adRequest = NativeAdRequest
            .Builder(adInfo.adUnitId, listOf(NativeAd.NativeAdType.NATIVE))
            .build()

        val adCallback =
            object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    ALog.d(TAG, "${adInfo.adTAG} loaded")
                    mNativeAds.add(nativeAd)
                    loadState = AdLoadState.Loaded(this@NativeAdManager)
                    loadListenerInternal?.onAdLoaded()
                    adLoadListener?.onAdLoaded()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    ALog.d(TAG, "${adInfo.adTAG} failed to load ${adError.message}")
                    val exception = Exception(adError.message)
                    loadState = AdLoadState.Failed(exception)
                    loadListenerInternal?.onAdFailed(exception)
                    adLoadListener?.onAdFailed(exception)
                }
            }

        NativeAdLoader.load(adRequest, adCallback)
    }

    override fun show(activity: Activity, adShowListener: AdShowListener) {}

    override fun isLoaded(): Boolean {
        return mNativeAds.isNotEmpty()
    }

    override fun destroy() {
        mNativeAds.forEach {
            it.destroy()
        }
        loadState = AdLoadState.Destroyed
    }

    override fun getLoadedAd() = mNativeAds.removeFirstOrNull()?.also {
        it.adEventCallback = object : NativeAdEventCallback {

            override fun onAdPaid(value: AdValue) {
                val extraMap: MutableMap<String, Any> = mutableMapOf()
                AdmobNetworkApp.instance.revenueListener.onRevenue(
                    revenueModel = AdmobRevenue(
                        adValue = value,
                        extras = extraMap,
                        adUnitId = adInfo.adUnitId,
                        adType = AdmobAdType.NATIVE,
                    )
                )
            }
        }
    }?.let { AdmobAppNativeAd(it) }

    companion object {

        private const val TAG = "Admob Native"
    }
}