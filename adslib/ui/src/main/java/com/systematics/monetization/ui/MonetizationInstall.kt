package com.systematics.monetization.ui

import android.util.Log
import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.models.ad.remote.group.abs.RemoteAdInfoGroup
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.ui.placement.AppAdsPlacements
import com.systematics.monetization.ui.remote.MonetizationRemote
import com.systematics.monetization.ui.utils.monetizationInject

class MonetizationInstall(
    private val adsCoreRemoteConfigs: AdsCoreRemoteConfigs,
    private val monetizationRemote: MonetizationRemote,
) {
    private val monetizationApp: MonetizationApp by monetizationInject()
    private val appAdsPlacements: AppAdsPlacements by monetizationInject()
    private val appAdsTimer: AppAdsTimer by monetizationInject()

    private val sdkDeferredAdInfos: MutableList<String> = mutableListOf()

    fun deferredInstall() {
        Log.d(TAG, "deferredInstall: size ${sdkDeferredAdInfos.size}")
        monetizationRemote.validateAdConfig()
        sdkDeferredAdInfos.forEach { loadAd(it) }
        sdkDeferredAdInfos.clear()
    }

    // Remote
    fun updateFromRemote() {
        if (!isInstallAllowed()) return
        if (monetizationApp.isInitialized.value) {
            monetizationRemote.updateFromRemote()
            ALog.d(TAG, "updateFromRemote: ads plan updated from remote")
        } else {
            ALog.d(TAG, "updateFromRemote: default ads plan")
        }
    }

    // BreakPoints
    fun executeBreakPoint(adBreakPoint: String) {
        if (!isInstallAllowed()) return
        val breakpointInfo = monetizationRemote.getBreakPoint(adBreakPoint)
        if (breakpointInfo != null) {
            ALog.d(TAG, "breakpoint $adBreakPoint triggered")
            breakpointInfo.adgroupBreakPoints.forEach {
                ALog.d(
                    TAG,
                    "breakpoint $adBreakPoint adgroup ${it.adGroupType} with placements ${it.placementKeys}"
                )
                loadAd(it.adGroupType, *it.placementKeys.toTypedArray())
            }
            breakpointInfo.timersToStart.forEach {
                ALog.d(TAG, "breakpoint $adBreakPoint timer ${it.timer} reached ${it.isReached}")
                appAdsTimer.startTimerForPlacement(it.timer, isReached = it.isReached)
            }
        }
    }


    // Pre load
    fun loadAd(adType: String, vararg placements: String) {
        if (placements.isNotEmpty()) {
            val anyPlacementAllowed =
                placements.map { appAdsPlacements.isPlacementAllowed(it) }.any { it }
            if (anyPlacementAllowed) {
                loadAppAd(adType)
            } else {
                ALog.d(TAG, "placement not allowed for group: $adType")
            }
        } else {
            loadAppAd(adType)
        }
    }


    private fun loadAppAd(adType: String) {
        val adInfoGroup = monetizationRemote.getRemoteAdConfig(adType)
        if (adInfoGroup == null) {
            Log.d(TAG, "loadAppAd: no such ad group found")
            return
        }
        if (!monetizationApp.isInitialized.value) {
            if (adInfoGroup.repeat) {
                if (sdkDeferredAdInfos.contains(adType)) {
                    Log.d(TAG, "loadAppAd: $adType already deferred")
                } else {
                    Log.d(TAG, "loadAppAd: deferring $adType")
                    sdkDeferredAdInfos.add(adType)
                }
            } else {
                Log.d(TAG, "loadAppAd: ${adInfoGroup.adType} skipped. no sdk initialized")
            }
            return
        }
        loadAdGroup(adInfoGroup)
    }

    // Load Ad Group
    private fun loadAdGroup(adModel: RemoteAdInfoGroup) {
        if (!isInstallAllowed()) return
        val adGroupInfo = adModel.toAdInfoGroup(monetizationApp.integrationManager)
        Log.d(TAG, "loadAdGroup: loading ${adModel.adType}")
        monetizationApp.adClient.loadAdGroup(adGroupInfo)
    }

    // Helpers
    private fun isInstallAllowed(): Boolean {
        return when {
            !adsCoreRemoteConfigs.monetizationEnabled() -> {
                Log.d(TAG, "isInstallAllowed: monetization not enabled")
                false
            }

            else -> true
        }
    }

    companion object {

        private const val TAG = "MonetizationInstall"
    }
}