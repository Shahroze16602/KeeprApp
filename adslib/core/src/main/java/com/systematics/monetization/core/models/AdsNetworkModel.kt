package com.systematics.monetization.core.models

import com.systematics.monetization.core.integration.interfaces.AdsNetworkIntegration
import com.systematics.monetization.core.models.enums.AdsNetworkInitStrategy

data class AdsNetworkModel(
    val adsNetwork: AdsNetworkIntegration,
    val initStrategy: AdsNetworkInitStrategy
)
