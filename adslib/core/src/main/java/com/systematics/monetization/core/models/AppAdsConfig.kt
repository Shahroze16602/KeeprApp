package com.systematics.monetization.core.models

import com.systematics.monetization.core.integration.IntegrationManager
import com.systematics.monetization.core.models.ad.remote.group.abs.RemoteAdInfoGroup
import com.systematics.monetization.core.utils.MonetizationSharedConfig
import kotlinx.serialization.Serializable

private const val TAG = "AdInfoPriorityModelTAG"

@Serializable
data class AppAdsConfig(
    val appFullScreens: Map<String, RemoteAdInfoGroup>,
    val appNatives: Map<String, RemoteAdInfoGroup>
) {

    fun filterForDebug(integrationManager: IntegrationManager): AppAdsConfig {
        return if (MonetizationSharedConfig.isDebug) {
            this.copy(
                appFullScreens = appFullScreens.mapValues {
                    it.value.filterForDebug(
                        integrationManager
                    )
                },
                appNatives = appNatives.mapValues { it.value.filterForDebug(integrationManager) }
            )
        } else {
            return this
        }
    }

    fun filterForKnownAdTypes(integrationManager: IntegrationManager): AppAdsConfig {
        return this.copy(
            appFullScreens = appFullScreens.mapValues {
                it.value.filterForKnownAdTypes(
                    integrationManager
                )
            },
            appNatives = appNatives.mapValues { it.value.filterForKnownAdTypes(integrationManager) }
        )
    }
}








