package com.systematics.monetization.core.remote

import kotlinx.coroutines.flow.StateFlow

interface AdsCoreRemoteConfigs {

    val monetizationEnabledState: StateFlow<Boolean>

    val initialRepeatDelayMillis: () -> Long
    val maxRepeatDelayMillis: () -> Long
    val repeatDelayMultiplier: () -> Double
    val isPlacementOffForcefully: (String) -> Boolean
    val monetizationEnabled: () -> Boolean
}