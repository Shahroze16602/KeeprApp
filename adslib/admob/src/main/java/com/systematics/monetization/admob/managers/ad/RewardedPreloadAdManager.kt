package com.systematics.monetization.admob.managers.ad

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import com.systematics.monetization.admob.AdmobNetworkApp
import com.systematics.monetization.admob.interfaces.RewardedPreloadAdCallback
import com.systematics.monetization.admob.repo.AdsPreloadManager
import com.systematics.monetization.admob.revenue.models.AdmobRevenue
import com.systematics.monetization.admob.utils.AdmobAdType
import com.systematics.monetization.core.exceptions.RewardNotEarnedException
import com.systematics.monetization.core.interfaces.AdShowListener
import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.models.AdLoadState
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.EventsConstants.AD_NOT_LOADED
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class RewardedPreloadAdManager(
    context: Context,
    adInfo: AdInfo
) : AdManager<RewardedAd>(context, adInfo) {
    private var mRewardedAd: RewardedAd? = null
    private var rewardEarned: Boolean = false

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
        AdsPreloadManager.load(
            adUnitId = adInfo.adUnitId,
            adType = adInfo.adType,
            object : RewardedPreloadAdCallback {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    ALog.d(TAG, "${adInfo.adTAG} failed to load ${adError.message}")
                    mRewardedAd = null
                    val exception = Exception(adError.message)
                    loadState = AdLoadState.Failed(exception)
                    loadListenerInternal?.onAdFailed(exception)
                    adLoadListener?.onAdFailed(exception)
                }

                override fun onAdLoaded(rewardedAd: RewardedAd) {
                    ALog.d(TAG, "${adInfo.adTAG} loaded")
                    mRewardedAd = rewardedAd
                    loadState = AdLoadState.Loaded(this@RewardedPreloadAdManager)
                    loadListenerInternal?.onAdLoaded()
                    adLoadListener?.onAdLoaded()
                }
            }
        )
    }

    override fun show(activity: Activity, adShowListener: AdShowListener) {
        mRewardedAd?.let {
            ALog.d(TAG, "${adInfo.adTAG} showing")
            it.adEventCallback = object : RewardedAdEventCallback {

                override fun onAdDismissedFullScreenContent() {
                    MainScope().launch {
                        ALog.d(TAG, "${adInfo.adTAG} shown")
                        loadState = AdLoadState.Shown
                        if (rewardEarned) {
                            adShowListener.onAdShown()
                        } else {
                            adShowListener.adShowingFailed(RewardNotEarnedException("No reward earned"))
                        }
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
                            adType = AdmobAdType.REWARDED
                        )
                    )
                }
            }

            it.show(activity) {
                ALog.d(TAG, "${adInfo.adTAG} reward earned")
                rewardEarned = true
            }

        } ?: run {
            Log.d(TAG, "show: ad not loaded yet to show")
            adShowListener.adShowingFailed(Exception(AD_NOT_LOADED))
        }
    }

    override fun destroy() {
        mRewardedAd = null
        loadState = AdLoadState.Destroyed
    }

    override fun getLoadedAd() = mRewardedAd

    companion object {

        private const val TAG = "Admob RewardedPreload"
    }
}