package com.systematics.monetization.core.managers.load

import android.content.Context
import android.util.Log
import com.systematics.monetization.core.analytics.AdsEvents
import com.systematics.monetization.core.exceptions.AdLoadException
import com.systematics.monetization.core.extensions.afterDelayedRepeat
import com.systematics.monetization.core.interfaces.IAdGroupLoadManager
import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.models.AdManagerResult
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.models.ad.local.group.PriorityAdInfoGroup
import com.systematics.monetization.core.utils.EventsConstants
import com.systematics.monetization.core.utils.EventsConstants.MATCHED_MANUAL
import com.systematics.monetization.core.utils.EventsConstants.MATCHED_MANUAL_2
import com.systematics.monetization.core.utils.createAdManager
import com.systematics.monetization.core.utils.isNetworkAvailable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class PriorityAdGroupLoadManager(
    override val adInfoGroup: PriorityAdInfoGroup,
    private val context: Context,
    private val analyticsLogger: (AdsEvents) -> Unit,
) : IAdGroupLoadManager {

    private val priorityAdManagers: MutableMap<AdInfo, AdManager<*>> = mutableMapOf()
    private val defaultAdManagers: MutableMap<AdInfo, AdManager<*>> = mutableMapOf()

    override val managerScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override var adLoadListener: AdManager.AdLoadListener? = null
        set(value) {
            field = value
            field?.let {
                if (isPriorityAdLoaded()) { // Priority Ad loaded
                    it.onAdLoaded()
                }
            }
        }

    override val isRepeatable: Boolean
        get() = adInfoGroup.repeatInfo.repeat

    override val timedDebounce: Boolean
        get() = adInfoGroup.repeatInfo.timedDebounce

    override val preserveWhileWorking: Boolean = false
    override val parkOnShown: Boolean = adInfoGroup.parkAfterImpression

    override fun loadAds() {
        if (isPriorityAdLoaded()) { // priority ad already loaded
            Log.d(TAG, "loadAds: priority ad already loaded for ${adInfoGroup.adType}")
            return
        }
        if (isDefaultAdLoaded()) { // default ad already loaded
            Log.d(TAG, "loadAds: default ad already loaded for ${adInfoGroup.adType}")
            return
        }
        if (isPriorityAdLoading()) { // priority loading
            Log.d(TAG, "loadAds: priority ad already loading for ${adInfoGroup.adType}")
            return
        }
        if (isDefaultAdLoading()) { // default loading
            Log.d(TAG, "loadAds: default ad already loading for ${adInfoGroup.adType}")
            return
        }
        Log.d(
            TAG,
            "loadAds: no ad loaded or loading. loading new  for ${adInfoGroup.adType}"
        )
        managerScope.launch { loadPriorityBasedAds() }
        managerScope.launch { loadDefaultAd() }
    }

    override fun isAdLoaded(): Boolean {
        return isPriorityAdLoaded() || isDefaultAdLoaded()
    }

    override fun isManualAdLoaded(): Boolean {
        return isPriorityAdLoaded() && priorityAdManagers.any {
            it.key.matchedTAG in listOf(MATCHED_MANUAL_2, MATCHED_MANUAL)
                    && it.value.isLoaded()
        }
    }

    override fun isAdLoading(): Boolean {
        return isPriorityAdLoading() || isDefaultAdLoading()
    }

    override fun getLoadedAdManager(): AdManagerResult? {
        adInfoGroup.priorityAdUnits.forEach {
            val unitAd = priorityAdManagers[it]
            if (unitAd != null && unitAd.isLoaded()) {
                Log.d(TAG, "getLoadedAdManager: sending priority manager ${unitAd.adInfo.adTAG}")
                analyticsLogger(AdsEvents(unitAd.adInfo.matchedTAG))
                return AdManagerResult(it, unitAd)
            }
        }
        adInfoGroup.defaultAdUnit.forEach {
            val unitAd = defaultAdManagers[it]
            if (unitAd != null && unitAd.isLoaded()) {
                Log.d(TAG, "getLoadedAdManager: sending default manager ${unitAd.adInfo.adTAG}")
                analyticsLogger(AdsEvents(unitAd.adInfo.matchedTAG))
                return AdManagerResult(it, unitAd)
            }
        }
        Log.d(TAG, "getLoadedAdManager: no ad available for ${adInfoGroup.adType}")
        return null
    }

    override fun removeThisAd(adInfo: AdInfo) {
        if (priorityAdManagers.keys.contains(adInfo)) {
            priorityAdManagers.remove(adInfo)
            Log.d(TAG, "removeThisAd: removed priority ${adInfo.adTAG}")
            if (!isPriorityAdLoaded() && adInfoGroup.repeatInfo.repeat) {
                if (adInfoGroup.repeatInfo.repeat) {
                    Log.d(TAG, "removeThisAd: loading priority ads after removing")
                    managerScope.launch { loadPriorityBasedAds() }
                    analyticsLogger(AdsEvents(EventsConstants.PRIORITY_RELOAD_REQUESTED) {
                        putString(EventsConstants.AD_TYPE, adInfoGroup.adType)
                    })
                }
            }
            return
        } else if (defaultAdManagers.keys.contains(adInfo)) {
            defaultAdManagers.remove(adInfo)
            Log.d(
                TAG,
                "removeThisAd: removed default ${adInfo.adTAG}"
            )
            if (adInfoGroup.repeatInfo.repeat) {
                managerScope.launch { loadAds() }
            }
        }
    }


    private fun isPriorityAdLoaded(): Boolean {
        if (priorityAdManagers.isEmpty()) return false
        return priorityAdManagers.any { it.value.isLoaded() }
    }

    private fun isDefaultAdLoaded(): Boolean {
        if (defaultAdManagers.isEmpty()) return false
        return defaultAdManagers.any { it.value.isLoaded() }
    }

    private fun isPriorityAdLoading(): Boolean {
        if (priorityAdManagers.isEmpty()) return false
        return priorityAdManagers.any { it.value.isLoading() }
    }

    private fun isDefaultAdLoading(): Boolean {
        if (defaultAdManagers.isEmpty()) return false
        return defaultAdManagers.any { it.value.isLoading() }
    }

    private suspend fun loadDefaultAd() {
        adInfoGroup.defaultAdUnit.forEach {
            try {
                Log.d(TAG, "loadDefaultAd: loading ${it.adTAG}")
                loadSpecificPriorityAd(it, false)
                Log.d(TAG, "loadDefaultAd: loaded ${it.adTAG}")
                return
            } catch (ex: AdLoadException) {
                Log.d(TAG, "loadDefaultAd: failed ${it.adTAG}")
            }
        }
        if (!isPriorityAdLoaded() && !isPriorityAdLoading()) { // default & priority all failed to load
            onAllAdsLoadingFailed()
        }
    }

    private suspend fun loadPriorityBasedAds() {
        adInfoGroup.priorityAdUnits.forEach {
            try {
                Log.d(TAG, "loadPriorityBasedAds: loading ${it.adTAG}")
                loadSpecificPriorityAd(it, true)
                Log.d(TAG, "loadPriorityBasedAds: loaded ${it.adTAG}")
                adLoadListener?.onAdLoaded()
                return
            } catch (ex: AdLoadException) {
                Log.d(TAG, "loadPriorityBasedAds: failed ${it.adTAG}")
            }
        }
        if (isDefaultAdLoaded()) { // default loaded and sending that
            adLoadListener?.onAdLoaded()
        } else { // all ads failed to load
            if (!isDefaultAdLoading()) {
                onAllAdsLoadingFailed()
            }
        }
    }

    private suspend fun loadSpecificPriorityAd(
        adInfo: AdInfo,
        addToPriority: Boolean
    ) {
        val adManager = createAdManager(adInfo)
        suspendCancellableCoroutine {
            if (addToPriority) {
                priorityAdManagers[adInfo] = adManager
            } else {
                defaultAdManagers[adInfo] = adManager
            }
            adManager.load(object : AdManager.AdLoadListener {
                override fun onAdLoaded() {
                    it.resume(adManager)
                }

                override fun onAdFailed(ex: Exception) {
                    it.cancel(AdLoadException(ex.message ?: EventsConstants.CAUSE_UNKNOWN))
                }
            })
        }
    }

    private fun onAllAdsLoadingFailed() {
        val failedEvent =
            if (isNetworkAvailable()) EventsConstants.PRIORITY_GROUP_FAILED else EventsConstants.PRIORITY_GROUP_NETWORK_FAILED
        adLoadListener?.onAdFailed(Exception(failedEvent))
        analyticsLogger(
            AdsEvents(failedEvent) {
                putString(EventsConstants.AD_TYPE, adInfoGroup.adType)
            }
        )
        if (adInfoGroup.repeatInfo.repeat && adInfoGroup.repeatInfo.timedDebounce) {
            if (isNetworkAvailable()){
                managerScope.launch {
                    adInfoGroup.repeatInfo.afterDelayedRepeat { loadAds() }
                }
            }
        }
    }

    override fun destroy() {
        Log.d(TAG, "destroy: destroying ${adInfoGroup.adType} ${adInfoGroup.adTAG}")
        managerScope.cancel(
            "Ad Group destroyed",
            InterruptedException("Ad group destroyed for ${adInfoGroup.adType}")
        )
    }

    companion object {

        private const val TAG = "PriorityAdGroupLoadManagerTAG"
    }
}