package com.systematics.monetization.ui.placement

import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.ui.placement.models.BannerPlacementModel
import com.systematics.monetization.ui.remote.MonetizationRemote

class AppAdsPlacements(
    private val monetizationRemote: MonetizationRemote,
    private val adsCoreRemoteConfigs: AdsCoreRemoteConfigs
) {

    fun isPlacementAllowed(placement: String): Boolean {
        if (!adsCoreRemoteConfigs.monetizationEnabled()) return false
        if (adsCoreRemoteConfigs.isPlacementOffForcefully(placement)) return false
        val fullScreenPlacement = monetizationRemote.appAdsUiConfig.appFullScreens[placement]
        val inlinePlacement = monetizationRemote.appAdsUiConfig.appInlines[placement]

        return fullScreenPlacement?.isEnabled ?: run {
            inlinePlacement?.let { it.isEnabled && it !is BannerPlacementModel } ?: false
        }
    }

    companion object {

        private const val TAG = "AppAdsPlacementsTAG"
    }
}