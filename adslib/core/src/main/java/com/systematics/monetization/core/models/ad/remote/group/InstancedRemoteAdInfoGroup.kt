package com.systematics.monetization.core.models.ad.remote.group

import androidx.annotation.Keep
import com.systematics.monetization.core.integration.IntegrationManager
import com.systematics.monetization.core.models.AdRepeatInfo
import com.systematics.monetization.core.models.ad.local.group.InstancedAdInfoGroup
import com.systematics.monetization.core.models.ad.remote.RemoteAdInfo
import com.systematics.monetization.core.models.ad.remote.group.abs.RemoteAdInfoGroup
import com.systematics.monetization.core.utils.MonetizationSharedConfig
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class InstancedRemoteAdInfoGroup(
    val instancedAds: List<RemoteAdInfo>,
    override val adType: String,
    override val adTAG: String,
    override val singletonAd: Boolean = false,
    override val parkAfterImpression: Boolean = false,
    override val repeat: Boolean = true,
    override val timedDebounce: Boolean = true,
) : RemoteAdInfoGroup() {

    override fun filterForDebug(integrationManager: IntegrationManager): InstancedRemoteAdInfoGroup {
        return if (MonetizationSharedConfig.isDebug) {
            this.copy(
                instancedAds = instancedAds.map { it.filterForDebug(integrationManager) },
            )
        } else {
            return this
        }
    }

    override fun filterForKnownAdTypes(integrationManager: IntegrationManager): RemoteAdInfoGroup {
        return this.copy(
            instancedAds = instancedAds.filter { it.adType in integrationManager.supportedAdTypes() }
        )
    }

    override fun toAdInfoGroup(integrationManager: IntegrationManager): InstancedAdInfoGroup {
        return InstancedAdInfoGroup(
            instancedAdUnits = instancedAds.map { it.toAdInfo() },
            adType = adType,
            adTAG = adTAG,
            singletonAd = singletonAd,
            parkAfterImpression = parkAfterImpression,
            repeatInfo = AdRepeatInfo(repeat, timedDebounce)
        )
    }
}
