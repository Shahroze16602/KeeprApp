package com.systematics.monetization.core.models

import com.systematics.monetization.core.MonetizationApp

data class AdRepeatInfo(
    val repeat: Boolean = false,
    val timedDebounce: Boolean = true,
    var currentRepeatMillis: Long = MonetizationApp.instance.adsCoreRemoteConfigs.initialRepeatDelayMillis(),
    val finalRepeatMillis: Long = MonetizationApp.instance.adsCoreRemoteConfigs.maxRepeatDelayMillis(),
    val multiplier: Double = MonetizationApp.instance.adsCoreRemoteConfigs.repeatDelayMultiplier()
)