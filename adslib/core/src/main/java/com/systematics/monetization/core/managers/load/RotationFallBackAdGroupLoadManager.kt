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
import com.systematics.monetization.core.models.ad.local.group.RotatedFallBackAdInfoGroup
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

internal class RotationFallBackAdGroupLoadManager(
    override val adInfoGroup: RotatedFallBackAdInfoGroup,
    private val context: Context,
    private val analyticsLogger: (AdsEvents) -> Unit,
) : IAdGroupLoadManager {

    private val rotationAdGroups = adInfoGroup.rotatedAdUnits.toMutableList()
    private val fallBackAdGroups = adInfoGroup.fallbackAdUnits.toMutableList()

    private val rotatedAdManagers: MutableMap<AdInfo, AdManager<*>> = mutableMapOf()
    private val fallbackAdManagers: MutableMap<AdInfo, AdManager<*>> = mutableMapOf()

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
        if (isRotatedAdLoaded()) {
            Log.d(TAG, "loadAds: rotated ad already loaded for ${adInfoGroup.adType} ")
            return
        }
        if (isFallbackAdLoaded()) {
            Log.d(TAG, "loadAds: fallback ad already loaded for ${adInfoGroup.adType}")
            return
        }
        if (isRotatedAdLoading()) {
            Log.d(TAG, "loadAds: rotated ad already loading for ${adInfoGroup.adType}")
            return
        }
        if (isFallbackAdLoading()) {
            Log.d(TAG, "loadAds: fallback ad already loading for ${adInfoGroup.adType}")
            return
        }
        Log.d(TAG, "loadAds: no ad loaded or loading. loading new for ${adInfoGroup.adType}")
        managerScope.launch { loadRotationBasedAds() }
    }

    override fun isAdLoaded(): Boolean {
        return isRotatedAdLoaded() || isFallbackAdLoaded()
    }

    override fun isManualAdLoaded(): Boolean {
        return false
    }

    override fun isAdLoading(): Boolean {
        return isRotatedAdLoading() || isFallbackAdLoading()
    }

    override fun getLoadedAdManager(): AdManagerResult? {
        adInfoGroup.rotatedAdUnits.forEach {
            val unitAd = rotatedAdManagers[it]
            if (unitAd != null && unitAd.isLoaded()) {
                Log.d(
                    TAG,
                    "getLoadedAdManager: sending rotated manager ${adInfoGroup.adType} ${unitAd.adInfo.adTAG}"
                )
                analyticsLogger(AdsEvents(unitAd.adInfo.matchedTAG))
                return AdManagerResult(it, unitAd)
            }
        }
        adInfoGroup.fallbackAdUnits.forEach {
            val unitAd = fallbackAdManagers[it]
            if (unitAd != null && unitAd.isLoaded()) {
                Log.d(
                    TAG,
                    "getLoadedAdManager: sending fallback manager ${adInfoGroup.adType} ${unitAd.adInfo.adTAG}"
                )
                analyticsLogger(AdsEvents(unitAd.adInfo.matchedTAG))
                return AdManagerResult(it, unitAd)
            }
        }
        Log.d(
            TAG,
            "getLoadedAdManager: no ad available for ${adInfoGroup.adType} ${adInfoGroup.adType}"
        )
        return null
    }

    override fun removeThisAd(adInfo: AdInfo) {
        if (rotatedAdManagers.keys.contains(adInfo)) {
            rotatedAdManagers.remove(adInfo)
            Log.d(TAG, "removeThisAd: removed rotated ${adInfoGroup.adType} ${adInfo.adTAG}")
            rotateAds(adInfo)
        } else if (fallbackAdManagers.keys.contains(adInfo)) {
            fallbackAdManagers.remove(adInfo)
            Log.d(
                TAG,
                "removeThisAd: removed fallback ${adInfoGroup.adType} ${adInfo.adTAG}"
            )
        }
        if (adInfoGroup.repeatInfo.repeat) {
            managerScope.launch { loadAds() }
        }
    }


    private fun isRotatedAdLoaded(): Boolean {
        if (rotatedAdManagers.isEmpty()) return false
        return rotatedAdManagers.any { it.value.isLoaded() }
    }

    private fun isFallbackAdLoaded(): Boolean {
        if (fallbackAdManagers.isEmpty()) return false
        return fallbackAdManagers.any { it.value.isLoaded() }
    }

    private fun isRotatedAdLoading(): Boolean {
        if (rotatedAdManagers.isEmpty()) return false
        return rotatedAdManagers.any { it.value.isLoading() }
    }

    private fun isFallbackAdLoading(): Boolean {
        if (fallbackAdManagers.isEmpty()) return false
        return fallbackAdManagers.any { it.value.isLoading() }
    }

    private suspend fun loadFallbackAd() {
        fallBackAdGroups.forEach {
            try {
                Log.d(TAG, "loadFallbackAd: loading ${adInfoGroup.adType} ${it.adTAG}")
                loadSpecificRotationAd(it, false)
                Log.d(TAG, "loadFallbackAd: loaded ${adInfoGroup.adType} ${it.adTAG}")
                adLoadListener?.onAdLoaded()
                return
            } catch (ex: AdLoadException) {
                Log.e(TAG, "loadFallbackAd: failed ${adInfoGroup.adType} ${it.adTAG}", ex)
            }
        }
        if (!isRotatedAdLoaded() && !isRotatedAdLoading()) { // rotated & fallback all failed to load
            onAllAdsLoadingFailed()
        }
    }

    private suspend fun loadRotationBasedAds() {
        rotationAdGroups.forEach {
            try {
                Log.d(TAG, "loadRotationBasedAds: loading ${adInfoGroup.adType} ${it.adTAG}")
                loadSpecificRotationAd(it, true)
                Log.d(TAG, "loadRotationBasedAds: loaded ${adInfoGroup.adType} ${it.adTAG}")
                adLoadListener?.onAdLoaded()
                return
            } catch (ex: AdLoadException) {
                Log.d(TAG, "loadRotationBasedAds: failed ${adInfoGroup.adType} ${it.adTAG}")
            }
        }
        if (isFallbackAdLoaded()) { // fallback loaded and sending that
            adLoadListener?.onAdLoaded()
        } else { // all ads failed to load
            if (!isFallbackAdLoading()) { // trigger fallback
                loadFallbackAd()
            }
        }
    }

    private suspend fun loadSpecificRotationAd(
        adInfo: AdInfo,
        addToRotated: Boolean
    ) {
        val adManager = createAdManager(adInfo)
        suspendCancellableCoroutine {
            if (addToRotated) {
                rotatedAdManagers[adInfo] = adManager
            } else {
                fallbackAdManagers[adInfo] = adManager
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

    private fun rotateAds(latestAdInfo: AdInfo) {
        rotationAdGroups.apply {
            remove(latestAdInfo)
            add(latestAdInfo)
        }
        Log.d(TAG, "rotateAds: applied rotation for ${adInfoGroup.adType} ${latestAdInfo.adTAG}")
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

        private const val TAG = "RotationFallbackAdGroupLoadManagerTAG"
    }
}