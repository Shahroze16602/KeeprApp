package com.systematics.monetization.core.models.ad.remote

import androidx.annotation.Keep
import com.systematics.monetization.core.integration.IntegrationManager
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.utils.MonetizationSharedConfig
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class RemoteAdInfo(
    val adUnitId: String = "",
    val adType: String = "",
    val adTAG: String = "",
    val matchedTAG: String = ""
) {

    fun filterForDebug(integrationManager: IntegrationManager): RemoteAdInfo {
        return if (MonetizationSharedConfig.isDebug) {
            this.copy(adUnitId = integrationManager.mapTestIds(adType, adUnitId))
        } else {
            this
        }
    }

    fun toAdInfo(): AdInfo {
        return AdInfo(
            adUnitId = adUnitId,
            adType = adType,
            adTAG = adTAG,
            matchedTAG = matchedTAG
        )
    }
}