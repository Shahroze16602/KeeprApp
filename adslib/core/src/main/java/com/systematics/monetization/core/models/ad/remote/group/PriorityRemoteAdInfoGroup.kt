package com.systematics.monetization.core.models.ad.remote.group

import androidx.annotation.Keep
import com.systematics.monetization.core.integration.IntegrationManager
import com.systematics.monetization.core.models.AdRepeatInfo
import com.systematics.monetization.core.models.ad.local.group.PriorityAdInfoGroup
import com.systematics.monetization.core.models.ad.remote.RemoteAdInfo
import com.systematics.monetization.core.models.ad.remote.group.abs.RemoteAdInfoGroup
import com.systematics.monetization.core.utils.MonetizationSharedConfig
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class PriorityRemoteAdInfoGroup(
    val manualAds: List<RemoteAdInfo>,
    val defaultAds: List<RemoteAdInfo>,
    override val adType: String,
    override val adTAG: String,
    override val repeat: Boolean = true,
    override val timedDebounce: Boolean = true,
    override val singletonAd: Boolean = false,
    override val parkAfterImpression: Boolean = false
) : RemoteAdInfoGroup() {

    override fun filterForDebug(integrationManager: IntegrationManager): PriorityRemoteAdInfoGroup {
        return if (MonetizationSharedConfig.isDebug) {
            this.copy(
                manualAds = manualAds.map { it.filterForDebug(integrationManager) },
                defaultAds = defaultAds.map { it.filterForDebug(integrationManager) }
            )
        } else {
            return this
        }
    }

    override fun filterForKnownAdTypes(integrationManager: IntegrationManager): RemoteAdInfoGroup {
        return this.copy(
            manualAds = manualAds.filter { it.adType in integrationManager.supportedAdTypes() },
            defaultAds = defaultAds.filter { it.adType in integrationManager.supportedAdTypes() }
        )
    }

    override fun toAdInfoGroup(integrationManager: IntegrationManager): PriorityAdInfoGroup {
        return PriorityAdInfoGroup(
            priorityAdUnits = manualAds.map { it.toAdInfo() },
            defaultAdUnit = defaultAds.map { it.toAdInfo() },
            adType = adType,
            adTAG = adTAG,
            singletonAd = singletonAd,
            parkAfterImpression = parkAfterImpression,
            repeatInfo = AdRepeatInfo(repeat, timedDebounce)
        )
    }
}