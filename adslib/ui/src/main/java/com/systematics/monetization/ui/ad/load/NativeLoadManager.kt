package com.systematics.monetization.ui.ad.load

import android.util.Log
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.models.natives.AppNativeAd
import com.systematics.monetization.core.models.natives.AppRefreshableNativeAd
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.loadAdSynced
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.ad.models.AdsShowRequestModel
import com.systematics.monetization.ui.ad.store.NativeAdStore
import com.systematics.monetization.ui.exceptions.AdNotPreloadedException
import com.systematics.monetization.ui.exceptions.MonetizationDisabledException
import com.systematics.monetization.ui.placement.RefreshMode
import com.systematics.monetization.ui.placement.models.NativePlacementModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NativeLoadManager(
    monetizationApp: MonetizationApp,
    private val monetizationInstall: MonetizationInstall,
    private val adsCoreRemoteConfigs: AdsCoreRemoteConfigs
) {
    private val adClient = monetizationApp.adClient

    fun getLoadedAd(
        coroutineScope: CoroutineScope,
        placementKey: String,
        refreshCount: Int = 0,
        nativeShown: Boolean = false,
        placement: NativePlacementModel,
        nativeAdStore: NativeAdStore,
        onNativeAdLoaded: (AppRefreshableNativeAd) -> Unit,
        onAdFailed: (Exception) -> Unit = {}
    ) {
        if (!adsCoreRemoteConfigs.monetizationEnabled()) {
            Log.d(TAG, "monetization disabled for placement ${placement.tag}")
            onAdFailed(MonetizationDisabledException())
            return
        }

        val preservedAd = nativeAdStore.getPreservedAd(placementKey)
        if (preservedAd != null) {
            if (preservedAd.refreshCount == refreshCount) {
                val refreshCountTag = if (refreshCount == 0) "" else " $refreshCount"
                Log.d(TAG, "preserved ad$refreshCountTag reused for ${placement.tag}")
                onNativeAdLoaded(preservedAd)
                return
            } else if (!nativeShown) {
                val refreshCountTag = if (refreshCount == 0) "" else " $refreshCount"
                Log.d(TAG, "preserved previous ad$refreshCountTag reused for ${placement.tag}")
                onNativeAdLoaded(preservedAd)
            }
        }

        val consumables = placement.consumableAdGroups.filter { adClient.isAdLoaded(it) }
        val consumableRequests = consumables.map { AdsShowRequestModel(it, true) }

        val currentAdGroupType = placement.adGroupType
        val currentLoaded = adClient.isAdLoaded(currentAdGroupType)

        val refreshMode = placement.refreshMode
        val refreshRequestModel = when (placement.refreshMode) {
            is RefreshMode.RefreshOnAction -> AdsShowRequestModel(
                refreshMode.refreshModel.adGroupType.ifEmpty { currentAdGroupType },
                refreshMode.refreshModel.isInstantAllowed
            )

            is RefreshMode.RefreshOnInterval -> AdsShowRequestModel(
                refreshMode.refreshModel.adGroupType.ifEmpty { currentAdGroupType },
                refreshMode.refreshModel.isInstantAllowed
            )

            is RefreshMode.RefreshOnResume -> AdsShowRequestModel(
                refreshMode.refreshModel.adGroupType.ifEmpty { currentAdGroupType },
                refreshMode.refreshModel.isInstantAllowed
            )

            else -> null
        }

        val loadedBackups = placement.backupAdGroups.filter { adClient.isAdLoaded(it.adGroupType) }
        val instantBackups = placement.backupAdGroups - loadedBackups.toSet()


        val tryRequests = mutableListOf<AdsShowRequestModel>().apply {
            addAll(consumableRequests)
        }.apply {
            if (refreshRequestModel != null && refreshCount != 0 && nativeShown) {
                add(refreshRequestModel)
            } else {
                if (placement.isInstantAllowed) {
                    add(AdsShowRequestModel(currentAdGroupType, true))
                } else {
                    if (currentLoaded) {
                        add(AdsShowRequestModel(currentAdGroupType, false))
                    }
                }
            }
            addAll(
                loadedBackups.map { AdsShowRequestModel(it.adGroupType, it.isInstantAllowed) }
            )
            addAll(
                instantBackups.map { AdsShowRequestModel(it.adGroupType, it.isInstantAllowed) }
            )
        }

        if (!placement.isInstantAllowed && tryRequests.isEmpty()) {
            ALog.d(TAG, "${placement.tag} failed to show, no preloaded ad")
            onAdFailed(AdNotPreloadedException("${placement.tag} failed to show, no preloaded ad"))
            return
        }

        loadTheseAdGroup(
            coroutineScope = coroutineScope,
            adTag = placement.tag,
            showRequestModels = tryRequests,
            onAdLoaded = { (showRequest, it) ->
                val refreshableNativeAd = AppRefreshableNativeAd(it, refreshCount)
                onNativeAdLoaded(refreshableNativeAd)
                if (placement.preserved) {
                    Log.d(
                        TAG,
                        "preserved ${showRequest.adGroupType} for ${placement.tag}"
                    )
                    nativeAdStore.preserveAd(placementKey, refreshableNativeAd)
                }
            },
            onAdFailed = onAdFailed
        )
    }

    fun loadTheseAdGroup(
        coroutineScope: CoroutineScope,
        adTag: String,
        showRequestModels: List<AdsShowRequestModel>,
        onAdLoaded: (Pair<AdsShowRequestModel, AppNativeAd>) -> Unit,
        onAdFailed: (Exception) -> Unit
    ) {
        val remainingShowRequests = showRequestModels.toMutableList()
        val currentShowRequest = MutableStateFlow(remainingShowRequests.first())

        coroutineScope.launch {
            currentShowRequest.collectLatest { currentRequest ->
                val isCurrentLoaded = adClient.isAdLoaded(currentRequest.adGroupType)
                val currentAllowed = if (isCurrentLoaded) {
                    true
                } else {
                    if (currentRequest.isInstantAllowed) {
                        monetizationInstall.loadAd(currentRequest.adGroupType)
                        true
                    } else {
                        false
                    }
                }
                if (currentAllowed) {
                    ALog.d(TAG, "$adTag placement requesting ${currentRequest.adGroupType}")
                    val adGroupMutex =
                        adGroupTypeLoadMutexMap.getOrPut(currentRequest.adGroupType) {
                            Mutex()
                        }
                    try {
                        adGroupMutex.withLock {
                            val loadedAdResult = loadAdSynced(
                                coroutineScope = coroutineScope,
                                adTag = adTag,
                                adType = currentRequest.adGroupType
                            )
                            val result = Pair(
                                currentRequest,
                                loadedAdResult.adManager.getLoadedAd() as AppNativeAd
                            )
                            Log.d(
                                TAG,
                                "$adTag placement succeed to load ${currentRequest.adGroupType}"
                            )
                            onAdLoaded(result)
                            adClient.onNativeAdShown(loadedAdResult)
                        }
                    } catch (ex: Exception) {
                        ALog.d(
                            TAG,
                            "$adTag placement failed to show ${currentRequest.adGroupType}"
                        )
                        remainingShowRequests.removeAt(0)
                        if (remainingShowRequests.isNotEmpty()) {
                            currentShowRequest.value = remainingShowRequests.first()
                        } else {
                            if (showRequestModels.size > 1) {
                                ALog.d(TAG, "$adTag placement failed to show all")
                            }
                            onAdFailed(ex)
                        }
                    }
                } else {
                    ALog.d(
                        TAG,
                        "$adTag placement request not allowed for ${currentRequest.adGroupType}"
                    )
                    remainingShowRequests.removeAt(0)
                    if (remainingShowRequests.isNotEmpty()) {
                        currentShowRequest.value = remainingShowRequests.first()
                    } else {
                        if (showRequestModels.size > 1) {
                            ALog.d(TAG, "$adTag placement failed to show all")
                        }
                        onAdFailed(AdNotPreloadedException("No preloaded ad for $adTag"))
                    }
                }
            }
        }
    }

    companion object {

        private val adGroupTypeLoadMutexMap: MutableMap<String, Mutex> = mutableMapOf()
        private const val TAG = "NativeLoadManagerTAG"
    }
}