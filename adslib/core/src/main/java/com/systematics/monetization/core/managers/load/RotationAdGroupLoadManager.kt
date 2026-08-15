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
import com.systematics.monetization.core.models.ad.local.group.RotatedAdInfoGroup
import com.systematics.monetization.core.utils.EventsConstants
import com.systematics.monetization.core.utils.EventsConstants.AD_TYPE
import com.systematics.monetization.core.utils.EventsConstants.GROUP_FAILED
import com.systematics.monetization.core.utils.EventsConstants.GROUP_NETWORK_FAILED
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

internal class RotationAdGroupLoadManager(
    override val adInfoGroup: RotatedAdInfoGroup,
    private val context: Context,
    private val analyticsLogger: (AdsEvents) -> Unit,
) : IAdGroupLoadManager {

    private val rotationAdGroups = adInfoGroup.adUnits.toMutableList()
    private val adManagers: MutableMap<AdInfo, AdManager<*>> = mutableMapOf()

    override val managerScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override var adLoadListener: AdManager.AdLoadListener? = null
        set(value) {
            field = value
            field?.let {
                if (isAdLoaded()) {
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
        if (isAdLoaded()) {
            Log.d(TAG, "loadAdNow: ad already loaded for ${adInfoGroup.adType}")
            return
        }
        Log.d(TAG, "loadAdNow: no ad loaded for ${adInfoGroup.adType}")
        managerScope.launch { loadAdsInQueue(rotationAdGroups) }
    }

    override fun isAdLoaded(): Boolean {
        if (adManagers.isEmpty()) return false
        return adManagers.any { it.value.isLoaded() }
    }

    override fun isManualAdLoaded(): Boolean {
        return false
    }

    override fun isAdLoading(): Boolean {
        if (adManagers.isEmpty()) return false
        return adManagers.any { it.value.isLoading() }
    }

    override fun getLoadedAdManager(): AdManagerResult? {
        rotationAdGroups.forEach {
            val unitAd = adManagers[it]
            if (unitAd != null && unitAd.isLoaded()) {
                Log.d(TAG, "getLoadedAdManager: sending loaded manager ${unitAd.adInfo.adTAG}")
                analyticsLogger(AdsEvents(unitAd.adInfo.matchedTAG))
                return AdManagerResult(it, unitAd)
            }
        }
        Log.d(TAG, "getLoadedAdManager: no ad available for ${adInfoGroup.adTAG}")
        return null
    }

    override fun removeThisAd(adInfo: AdInfo) {
        if (adManagers.keys.contains(adInfo)) {
            adManagers.remove(adInfo)
            Log.d(TAG, "removeThisAd: removed ${adInfo.adTAG}")
            rotateAds(adInfo)
            if (adInfoGroup.repeatInfo.repeat) {
                loadAds()
            }
        }
    }

    private fun rotateAds(latestAdInfo: AdInfo) {
        rotationAdGroups.apply {
            remove(latestAdInfo)
            add(latestAdInfo)
        }
        Log.d(TAG, "rotateAds: applied rotation for ${latestAdInfo.adTAG}")
    }

    private suspend fun loadAdsInQueue(adUnits: List<AdInfo>) {
        Log.d(
            TAG,
            "loadAdsInQueue: starting from queue sized ${adInfoGroup.adUnits.size} for ${adInfoGroup.adType}"
        )
        adUnits.forEach {
            try {
                Log.d(TAG, "loadAdsInQueue: loading ${it.adTAG}")
                loadThisAdUnit(it)
                Log.d(TAG, "loadAdsInQueue: loaded ${it.adTAG}")
                adLoadListener?.onAdLoaded()
                return
            } catch (ex: AdLoadException) {
                Log.e(TAG, "loadAdsInQueue: failed loading ${it.adTAG} ", ex)
            }
        }
        Log.d(TAG, "loadAdsInQueue: failed all")
        onAllAdsLoadingFailed()
    }

    private suspend fun loadThisAdUnit(
        adInfo: AdInfo
    ) {
        val adManager = createAdManager(adInfo)
        suspendCancellableCoroutine {
            adManagers[adInfo] = adManager
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
        val failedEvent = if (isNetworkAvailable()) GROUP_FAILED else GROUP_NETWORK_FAILED
        adLoadListener?.onAdFailed(Exception(failedEvent))
        analyticsLogger(
            AdsEvents(failedEvent) {
                putString(AD_TYPE, adInfoGroup.adType)
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

        private const val TAG = "RotationAdGroupLoadManagerTAG"
    }
}