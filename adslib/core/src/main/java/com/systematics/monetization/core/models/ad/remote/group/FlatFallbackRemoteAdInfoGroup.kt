package com.systematics.monetization.core.models.ad.remote.group

import androidx.annotation.Keep
import com.systematics.monetization.core.integration.IntegrationManager
import com.systematics.monetization.core.models.AdRepeatInfo
import com.systematics.monetization.core.models.ad.local.group.FlatFallbackAdInfoGroup
import com.systematics.monetization.core.models.ad.remote.RemoteAdInfo
import com.systematics.monetization.core.models.ad.remote.group.abs.RemoteAdInfoGroup
import com.systematics.monetization.core.utils.MonetizationSharedConfig
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FlatFallbackRemoteAdInfoGroup(
    val flatAds: List<RemoteAdInfo>,
    override val adType: String,
    override val adTAG: String,
    override val repeat: Boolean = true,
    override val timedDebounce: Boolean = true,
    override val singletonAd: Boolean = false,
    override val parkAfterImpression: Boolean = false
) : RemoteAdInfoGroup() {

    override fun filterForDebug(integrationManager: IntegrationManager): FlatFallbackRemoteAdInfoGroup {
        return if (MonetizationSharedConfig.isDebug) {
            this.copy(
                flatAds = flatAds.map { it.filterForDebug(integrationManager) },
            )
        } else {
            return this
        }
    }

    override fun filterForKnownAdTypes(integrationManager: IntegrationManager): RemoteAdInfoGroup {
        return this.copy(flatAds = flatAds.filter { it.adType in integrationManager.supportedAdTypes() })
    }

    override fun toAdInfoGroup(integrationManager: IntegrationManager): FlatFallbackAdInfoGroup {
        return FlatFallbackAdInfoGroup(
            adUnits = flatAds.map { it.toAdInfo() },
            adType = adType,
            adTAG = adTAG,
            singletonAd = singletonAd,
            parkAfterImpression = parkAfterImpression,
            repeatInfo = AdRepeatInfo(repeat, timedDebounce)
        )
    }
}