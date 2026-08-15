package com.systematics.monetization.core.managers.wrappers

import android.content.Context
import android.util.Log
import com.systematics.monetization.core.analytics.AdsEvents
import com.systematics.monetization.core.interfaces.AdGroupLoadWListener
import com.systematics.monetization.core.interfaces.IAdGroupLoadManager
import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.managers.load.FlatFallbackAdGroupLoadManager
import com.systematics.monetization.core.managers.load.InstancedAdGroupLoadManager
import com.systematics.monetization.core.managers.load.PriorityAdGroupLoadManager
import com.systematics.monetization.core.managers.load.RotationAdGroupLoadManager
import com.systematics.monetization.core.managers.load.RotationFallBackAdGroupLoadManager
import com.systematics.monetization.core.models.AdGroupResult
import com.systematics.monetization.core.models.ad.local.group.FlatFallbackAdInfoGroup
import com.systematics.monetization.core.models.ad.local.group.InstancedAdInfoGroup
import com.systematics.monetization.core.models.ad.local.group.PriorityAdInfoGroup
import com.systematics.monetization.core.models.ad.local.group.RotatedAdInfoGroup
import com.systematics.monetization.core.models.ad.local.group.RotatedFallBackAdInfoGroup
import com.systematics.monetization.core.models.ad.local.group.abs.AdInfoGroup
import com.systematics.monetization.core.utils.InternetController
import com.systematics.monetization.core.utils.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

internal class AdGroupLoadWrapper(
    private val context: Context,
    private val internetController: InternetController,
    private val analyticsLogger: (AdsEvents) -> Unit,
) {

    var adGroupLoadWListener: AdGroupLoadWListener? = null
    private val adGroupManagers: MutableMap<String, IAdGroupLoadManager> = mutableMapOf()
    private val singletonAdsHandled: MutableList<String> = mutableListOf()
    private val parkedAdGroupManagers: MutableMap<String, IAdGroupLoadManager> = mutableMapOf()
    private val internetFailedManagers: MutableList<IAdGroupLoadManager> = mutableListOf()

    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        coroutineScope.launch {
            internetController.observeInternet().collectLatest { isConnected ->
                if (isConnected) {
                    Log.d(
                        TAG,
                        "internet resumed: waiting failed managers ${internetFailedManagers.size}"
                    )
                    internetFailedManagers.forEach { it.loadAds() }
                    internetFailedManagers.clear()
                }
            }
        }
    }

    fun loadAdNow(adInfoGroup: AdInfoGroup) {
        if (isAdLoaded(adInfoGroup.adType)) {
            Log.d(TAG, "loadAdNow: already loaded ${adInfoGroup.adType}")
            return
        } else if (isAdLoading(adInfoGroup.adType)) {
            Log.d(TAG, "loadAdNow: already loading ${adInfoGroup.adType}")
        } else if (parkedAdGroupManagers.contains(adInfoGroup.adType)) {
            Log.d(TAG, "loadAdNow: found parked manager ${adInfoGroup.adType}")
            val parkedAdManager = parkedAdGroupManagers[adInfoGroup.adType]!!
            adGroupManagers[adInfoGroup.adType] = parkedAdManager
            parkedAdManager.loadAds()
        } else {
            if (adGroupManagers.contains(adInfoGroup.adType)) {
                Log.d(TAG, "loadAdNow: ${adInfoGroup.adType} already in load map")
            } else {
                if (adInfoGroup.singletonAd && singletonAdsHandled.contains(adInfoGroup.adType)) {
                    Log.d(
                        TAG,
                        "loadAdNow: singleton failed ad ${adInfoGroup.adType} already handled"
                    )
                } else {
                    createAdManager(adInfoGroup)?.let { adManager ->
                        adGroupManagers[adInfoGroup.adType] = adManager
                        adManager.adLoadListener = object : AdManager.AdLoadListener {
                            override fun onAdLoaded() {
                                Log.d(TAG, "onAdLoaded: loaded ${adInfoGroup.adType}")
                                adGroupLoadWListener?.onAdLoaded(adInfoGroup.adType) {
                                    adManager.getLoadedAdManager()?.let {
                                        AdGroupResult(
                                            adInfoGroup.adType,
                                            it.adInfo,
                                            it.adManager
                                        )
                                    }
                                }
                            }

                            override fun onAdFailed(ex: Exception) {
                                Log.e(TAG, "onAdFailed: ad load exception", ex)
                                adGroupLoadWListener?.onAdLoadFailed(adInfoGroup.adType, ex)
                                if (adInfoGroup.repeatInfo.repeat && adInfoGroup.repeatInfo.timedDebounce) {
                                    if (!isNetworkAvailable()) {
                                        Log.d(TAG, "onAdFailed: adding into internet failed list")
                                        internetFailedManagers.add(adManager)
                                    }
                                } else {
                                    adGroupManagers.remove(adInfoGroup.adType)
                                    Log.d(
                                        TAG,
                                        "onAdFailed: removing load manager for ${adInfoGroup.adType}"
                                    )
                                }
                                if (adInfoGroup.singletonAd) {
                                    singletonAdsHandled.add(adInfoGroup.adType)
                                    Log.d(
                                        TAG,
                                        "loadAdNow: handled singleton failed ad ${adInfoGroup.adType}"
                                    )
                                }
                            }
                        }
                        adManager.loadAds()
                        Log.d(TAG, "loadAdNow: loading ${adInfoGroup.adType}")
                    }
                }
            }
        }
    }

    fun destroyGroupTypeNow(adType: String) {
        val adInMap = adGroupManagers.contains(adType)
        val anyLoaded = adGroupManagers[adType]?.isAdLoaded() ?: false
        val manualLoaded = adGroupManagers[adType]?.isManualAdLoaded() ?: false
        Log.d(
            TAG,
            "isAdLoaded: in map -> $adInMap any loaded -> $anyLoaded manual loaded -> $manualLoaded $adType "
        )
        if (adInMap) {
            adGroupManagers[adType]?.destroy()?.let {
                Log.d(TAG, "destroyGroupTypeNow: destroyed $adType")
                adGroupManagers.remove(adType)
            }
        }
    }

    fun isAdLoaded(adType: String, manualOnly: Boolean = false): Boolean {
        val adInMap = adGroupManagers.contains(adType)
        val anyLoaded = adGroupManagers[adType]?.isAdLoaded() ?: false
        val manualLoaded = adGroupManagers[adType]?.isManualAdLoaded() ?: false
        Log.d(
            TAG,
            "isAdLoaded: in map -> $adInMap any loaded -> $anyLoaded manual loaded -> $manualLoaded $adType "
        )
        return if (manualOnly) manualLoaded else anyLoaded
    }

    fun isAdLoading(adType: String): Boolean {
        val adInMap = adGroupManagers.contains(adType)
        val adLoading = adGroupManagers[adType]?.isAdLoading() ?: false
        Log.d(TAG, "isAdLoading: in map -> $adInMap loading -> $adLoading $adType ")
        return adLoading
    }

    fun getLoadedAdsCount(adType: String): Int {
        val adInMap = adGroupManagers.contains(adType)
        val totalLoaded = adGroupManagers[adType]?.getLoadedAdsCount() ?: 0
        Log.d(
            TAG,
            "getLoadedAdsCount: in map -> $adInMap total loaded -> $totalLoaded $adType "
        )
        return totalLoaded
    }

    fun isLoadRequested(adType: String): Boolean = adGroupManagers.contains(adType)

    fun getLoadedAdGroupResult(adType: String): AdGroupResult? {
        val unitAd = adGroupManagers[adType]
        if (unitAd == null) {
            Log.d(TAG, "getAdManager: no ad available for $adType")
            return null
        }
        if (!unitAd.isAdLoaded()) {
            Log.d(TAG, "getAdManager: no ad loaded for $adType")
            return null
        }
        val loadedAdManager = unitAd.getLoadedAdManager()
        if (loadedAdManager == null) {
            Log.d(TAG, "getAdManager: no ad unit loaded for $adType")
            return null
        }
        Log.d(TAG, "getAdManager: sending ad manager for $adType")
        return AdGroupResult(
            unitAd.adInfoGroup.adType,
            loadedAdManager.adInfo,
            loadedAdManager.adManager
        )
    }

    fun onAdShown(adGroupResult: AdGroupResult) {
        Log.d(TAG, "onAdShown: shown ${adGroupResult.adType} ${adGroupResult.adInfo.adTAG}")
        if (adGroupManagers.contains(adGroupResult.adType)) {
            val adManager = adGroupManagers[adGroupResult.adType]!!
            adManager.removeThisAd(adGroupResult.adInfo)
            val isReusable =
                (adManager.preserveWhileWorking && (adManager.isAdLoaded() || adManager.isAdLoading()))
            if ((!adManager.isRepeatable && !isReusable) || adManager.parkOnShown) {
                adGroupManagers.remove(adGroupResult.adType)
                Log.d(
                    TAG,
                    "onAdShown: removed manager after showing ${adGroupResult.adType} ${adGroupResult.adInfo.adTAG}"
                )
            }
            if (adManager.parkOnShown) {
                Log.d(
                    TAG,
                    "onAdShown: parked after showing ${adGroupResult.adType} ${adGroupResult.adInfo.adTAG}"
                )
                parkedAdGroupManagers[adGroupResult.adType] = adManager
            }
        }
    }

    private fun createAdManager(adInfoGroup: AdInfoGroup): IAdGroupLoadManager? {
        return when (val validatedAdInfoGroup = adInfoGroup.validateAdInfoGroup()) {
            is RotatedAdInfoGroup -> RotationAdGroupLoadManager(
                validatedAdInfoGroup,
                context,
                analyticsLogger
            )

            is RotatedFallBackAdInfoGroup -> RotationFallBackAdGroupLoadManager(
                validatedAdInfoGroup,
                context,
                analyticsLogger
            )

            is FlatFallbackAdInfoGroup -> FlatFallbackAdGroupLoadManager(
                validatedAdInfoGroup,
                context,
                analyticsLogger
            )

            is PriorityAdInfoGroup -> PriorityAdGroupLoadManager(
                validatedAdInfoGroup,
                context,
                analyticsLogger
            )

            is InstancedAdInfoGroup -> InstancedAdGroupLoadManager(
                validatedAdInfoGroup,
                context,
                analyticsLogger
            )

            else -> null
        }
    }

    companion object {

        private const val TAG = "AdGroupLoadWrapperTAG"
    }
}