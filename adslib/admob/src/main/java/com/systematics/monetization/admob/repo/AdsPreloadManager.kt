package com.systematics.monetization.admob.repo

import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.systematics.monetization.admob.interfaces.AppOpenPreloadAdCallback
import com.systematics.monetization.admob.interfaces.InterPreloadAdCallback
import com.systematics.monetization.admob.interfaces.PreloadAdCallback
import com.systematics.monetization.admob.interfaces.RewardedPreloadAdCallback
import com.systematics.monetization.admob.managers.core.AdmobPreLoader
import com.systematics.monetization.admob.managers.preload.AppOpenAdsPreLoader
import com.systematics.monetization.admob.managers.preload.InterstitialAdsPreLoader
import com.systematics.monetization.admob.managers.preload.RewardedAdsPreLoader
import com.systematics.monetization.admob.utils.AdmobAdType
import java.util.LinkedList
import java.util.Queue

object AdsPreloadManager {

    private const val TAG = "AdsPreloadManagerTAG"

    private val adsPreLoaders: MutableMap<String, AdmobPreLoader<*>> = mutableMapOf()
    private val interCallbacks = mutableMapOf<String, Queue<InterPreloadAdCallback>>()
    private val appOpenCallbacks = mutableMapOf<String, Queue<AppOpenPreloadAdCallback>>()
    private val rewardedCallbacks = mutableMapOf<String, Queue<RewardedPreloadAdCallback>>()

    fun load(
        adUnitId: String,
        adType: String,
        preloadAdCallback: PreloadAdCallback
    ) {
        when (adType) {
            AdmobAdType.INTERSTITIAL_PRE_LOAD -> {
                (preloadAdCallback as? InterPreloadAdCallback)?.let {
                    interCallbacks.getOrPut(adUnitId) { LinkedList() }.add(it)
                }
                if (adsPreLoaders.contains(adUnitId)) {
                    Log.d(TAG, "load: $adUnitId already in process")
                    val manager = adsPreLoaders[adUnitId]!!
                    if (manager.isAdAvailable()) {
                        manager.pushAd()
                    }
                } else {
                    Log.d(TAG, "load: $adUnitId loading")
                    val preLoader = InterstitialAdsPreLoader(
                        adUnitId = adUnitId,
                        isAdNeeded = { interCallbacks.contains(it) },
                        onAdPreloaded = onAdPreloaded,
                        onAdFailed = onInterFailed,
                    )
                    adsPreLoaders[adUnitId] = preLoader
                }
            }

            AdmobAdType.APP_OPEN_PRE_LOAD -> {
                (preloadAdCallback as? AppOpenPreloadAdCallback)?.let {
                    appOpenCallbacks.getOrPut(adUnitId) { LinkedList() }.add(it)
                }
                if (adsPreLoaders.contains(adUnitId)) {
                    Log.d(TAG, "load: $adUnitId already in process")
                    val manager = adsPreLoaders[adUnitId]!!
                    if (manager.isAdAvailable()) {
                        manager.pushAd()
                    }
                } else {
                    Log.d(TAG, "load: $adUnitId loading")
                    val preLoader = AppOpenAdsPreLoader(
                        adUnitId = adUnitId,
                        isAdNeeded = { appOpenCallbacks.contains(it) },
                        onAdPreloaded = onAdPreloaded,
                        onAdFailed = onAppOpenFailed,
                    )
                    adsPreLoaders[adUnitId] = preLoader
                }
            }

            AdmobAdType.REWARDED_PRE_LOAD -> {
                (preloadAdCallback as? RewardedPreloadAdCallback)?.let {
                    rewardedCallbacks.getOrPut(adUnitId) { LinkedList() }.add(it)
                }
                if (adsPreLoaders.contains(adUnitId)) {
                    Log.d(TAG, "load: $adUnitId already in process")
                    val manager = adsPreLoaders[adUnitId]!!
                    if (manager.isAdAvailable()) {
                        manager.pushAd()
                    }
                } else {
                    Log.d(TAG, "load: $adUnitId loading")
                    val preLoader = RewardedAdsPreLoader(
                        adUnitId = adUnitId,
                        isAdNeeded = { rewardedCallbacks.contains(it) },
                        onAdPreloaded = onAdPreloaded,
                        onAdFailed = onRewardedFailed,
                    )
                    adsPreLoaders[adUnitId] = preLoader
                }
            }

            else -> {
                preloadAdCallback.onAdFailedToLoad(
                    LoadAdError(
                        LoadAdError.ErrorCode.INVALID_REQUEST,
                        "Ad type not supported",
                    )
                )
                null
            }
        }
    }

    private val onAdPreloaded: (String, Any) -> Unit = { id, ad ->
        if (ad is InterstitialAd) {
            interCallbacks[id]?.poll()?.onAdLoaded(ad)
            if (interCallbacks[id]?.isEmpty() == true) {
                interCallbacks.remove(id)
            }
        } else if (ad is AppOpenAd) {
            appOpenCallbacks[id]?.poll()?.onAdLoaded(ad)
            if (appOpenCallbacks[id]?.isEmpty() == true) {
                appOpenCallbacks.remove(id)
            }
        } else if (ad is RewardedAd) {
            rewardedCallbacks[id]?.poll()?.onAdLoaded(ad)
            if (rewardedCallbacks[id]?.isEmpty() == true) {
                rewardedCallbacks.remove(id)
            }
        }
    }

    private val onInterFailed: (String, LoadAdError) -> Unit = { id, adError ->
        interCallbacks[id]?.forEach { it.onAdFailedToLoad(adError) }
        interCallbacks[id]?.clear()
        interCallbacks.remove(id)
    }

    private val onAppOpenFailed: (String, LoadAdError) -> Unit = { id, adError ->
        appOpenCallbacks[id]?.forEach { it.onAdFailedToLoad(adError) }
        appOpenCallbacks[id]?.clear()
        appOpenCallbacks.remove(id)
    }

    private val onRewardedFailed: (String, LoadAdError) -> Unit = { id, adError ->
        rewardedCallbacks[id]?.forEach { it.onAdFailedToLoad(adError) }
        rewardedCallbacks[id]?.clear()
        rewardedCallbacks.remove(id)
    }
}
