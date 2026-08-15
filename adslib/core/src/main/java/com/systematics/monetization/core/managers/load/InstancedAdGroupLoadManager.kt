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
import com.systematics.monetization.core.models.ad.local.group.InstancedAdInfoGroup
import com.systematics.monetization.core.utils.EventsConstants
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

class InstancedAdGroupLoadManager(
    override val adInfoGroup: InstancedAdInfoGroup,
    private val context: Context,
    private val analyticsLogger: (AdsEvents) -> Unit,
) : IAdGroupLoadManager {

    init {

        Log.d(TAG, "created: ${adInfoGroup.instancedAdUnits.size}")
    }

    private val instancedAdManagers: MutableMap<AdInfo, AdManager<*>> = mutableMapOf()

    override val managerScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override var adLoadListener: AdManager.AdLoadListener? = null
        set(value) {
            field = value
            field?.let {
                if (isInstancedAdLoaded()) { // Instanced Ad loaded
                    it.onAdLoaded()
                }
            }
        }

    override val isRepeatable: Boolean
        get() = adInfoGroup.repeatInfo.repeat

    override val timedDebounce: Boolean
        get() = adInfoGroup.repeatInfo.timedDebounce

    override val preserveWhileWorking: Boolean = true
    override val parkOnShown: Boolean = adInfoGroup.parkAfterImpression

    override fun loadAds() {
        if (isInstancedAdLoaded()) { // instanced ad already loaded
            Log.d(TAG, "loadAds: instanced ad already loaded for ${adInfoGroup.adType}")
            return
        }
        if (isInstancedAdLoading()) { // instanced loading
            Log.d(TAG, "loadAds: instanced ad already loading for ${adInfoGroup.adType}")
            return
        }
        Log.d(
            TAG,
            "loadAds: no ad loaded or loading. loading new for ${adInfoGroup.adType}"
        )
        loadInstancedAd()
    }

    override fun isAdLoaded(): Boolean {
        return isInstancedAdLoaded()
    }

    override fun getLoadedAdsCount(): Int {
        return instancedAdManagers.filter { it.value.isLoaded() }.size
    }

    override fun isManualAdLoaded(): Boolean {
        return false
    }

    override fun isAdLoading(): Boolean {
        return isInstancedAdLoading()
    }

    override fun getLoadedAdManager(): AdManagerResult? {
        adInfoGroup.instancedAdUnits.forEach {
            val unitAd = instancedAdManagers[it]
            if (unitAd != null && unitAd.isLoaded()) {
                Log.d(TAG, "getLoadedAdManager: sending priority manager ${unitAd.adInfo.adTAG}")
                analyticsLogger(AdsEvents(unitAd.adInfo.matchedTAG))
                return AdManagerResult(it, unitAd)
            }
        }
        Log.d(TAG, "getLoadedAdManager: no ad available for ${adInfoGroup.adType}")
        return null
    }

    override fun removeThisAd(adInfo: AdInfo) {
        if (instancedAdManagers.keys.contains(adInfo)) {
            instancedAdManagers.remove(adInfo)
            Log.d(
                TAG,
                "removeThisAd: removed default ${adInfo.adTAG}"
            )
            if (adInfoGroup.repeatInfo.repeat) {
                managerScope.launch { loadAds() }
            }
        }
    }

    private fun isInstancedAdLoaded(): Boolean {
        if (instancedAdManagers.isEmpty()) return false
        return instancedAdManagers.any { it.value.isLoaded() }
    }

    private fun isInstancedAdLoading(): Boolean {
        if (instancedAdManagers.isEmpty()) return false
        return instancedAdManagers.any { it.value.isLoading() }
    }

    private fun loadInstancedAd() {
        adInfoGroup.instancedAdUnits.forEach {
            managerScope.launch {
                try {
                    Log.d(TAG, "loadInstancedAd: loading ${it.adTAG}")
                    loadSpecificInstancedAd(it)
                    Log.d(TAG, "loadInstancedAd: loaded ${it.adTAG}")
                    adLoadListener?.onAdLoaded()
                } catch (ex: AdLoadException) {
                    Log.d(TAG, "loadInstancedAd: failed ${it.adTAG}")
                    checkIfAllFailed()
                }
            }
        }
    }

    private suspend fun loadSpecificInstancedAd(adInfo: AdInfo) {
        val adManager = createAdManager(adInfo)
        suspendCancellableCoroutine {
            instancedAdManagers[adInfo] = adManager
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

    private fun checkIfAllFailed() {
        if (isInstancedAdLoaded()) {
            Log.d(TAG, "checkIfAllFailed: loading any ad")
        } else if (isInstancedAdLoading()) {
            Log.d(TAG, "checkIfAllFailed: loading any ad")
        } else {
            Log.d(TAG, "checkIfAllFailed: no ad loaded or loading. all failed")
            onAllAdsLoadingFailed()
        }
    }

    private fun onAllAdsLoadingFailed() {
        val failedEvent =
            if (isNetworkAvailable()) EventsConstants.GROUP_FAILED else EventsConstants.GROUP_NETWORK_FAILED
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

        private const val TAG = "InstancedAdGroupLoadManager"
    }
}