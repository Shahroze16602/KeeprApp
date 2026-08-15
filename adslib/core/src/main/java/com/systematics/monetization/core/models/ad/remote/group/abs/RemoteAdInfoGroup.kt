package com.systematics.monetization.core.models.ad.remote.group.abs

import androidx.annotation.Keep
import com.systematics.monetization.core.integration.IntegrationManager
import com.systematics.monetization.core.models.ad.local.group.abs.AdInfoGroup
import kotlinx.serialization.Serializable

@Keep
@Serializable
abstract class RemoteAdInfoGroup {
    abstract val adType: String
    abstract val adTAG: String
    abstract val singletonAd: Boolean
    abstract val parkAfterImpression: Boolean

    abstract val repeat: Boolean
    abstract val timedDebounce: Boolean

    abstract fun filterForDebug(integrationManager: IntegrationManager): RemoteAdInfoGroup
    abstract fun filterForKnownAdTypes(integrationManager: IntegrationManager): RemoteAdInfoGroup
    abstract fun toAdInfoGroup(integrationManager: IntegrationManager): AdInfoGroup
}