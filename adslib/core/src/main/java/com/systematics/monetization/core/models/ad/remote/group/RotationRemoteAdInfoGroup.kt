package com.systematics.monetization.core.models.ad.remote.group

import androidx.annotation.Keep
import com.systematics.monetization.core.integration.IntegrationManager
import com.systematics.monetization.core.models.AdRepeatInfo
import com.systematics.monetization.core.models.ad.local.group.RotatedAdInfoGroup
import com.systematics.monetization.core.models.ad.remote.RemoteAdInfo
import com.systematics.monetization.core.models.ad.remote.group.abs.RemoteAdInfoGroup
import com.systematics.monetization.core.utils.MonetizationSharedConfig
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class RotationRemoteAdInfoGroup(
    val rotatedAds: List<RemoteAdInfo>,
    override val adType: String,
    override val adTAG: String,
    override val repeat: Boolean = true,
    override val timedDebounce: Boolean = true,
    override val singletonAd: Boolean = false,
    override val parkAfterImpression: Boolean = false
) : RemoteAdInfoGroup() {

    override fun filterForDebug(integrationManager: IntegrationManager): RotationRemoteAdInfoGroup {
        return if (MonetizationSharedConfig.isDebug) {
            this.copy(
                rotatedAds = rotatedAds.map { it.filterForDebug(integrationManager) },
            )
        } else {
            return this
        }
    }

    override fun filterForKnownAdTypes(integrationManager: IntegrationManager): RemoteAdInfoGroup {
        return this.copy(rotatedAds = rotatedAds.filter { it.adType in integrationManager.supportedAdTypes() })
    }

    override fun toAdInfoGroup(integrationManager: IntegrationManager): RotatedAdInfoGroup {
        return RotatedAdInfoGroup(
            adUnits = rotatedAds.map { it.toAdInfo() },
            adType = adType,
            adTAG = adTAG,
            singletonAd = singletonAd,
            parkAfterImpression = parkAfterImpression,
            repeatInfo = AdRepeatInfo(repeat, timedDebounce)
        )
    }
}