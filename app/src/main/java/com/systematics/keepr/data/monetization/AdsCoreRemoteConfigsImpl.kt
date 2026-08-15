package com.systematics.keepr.data.monetization

import com.systematics.keepr.data.datasource.FirebaseConfigDataSource
import com.systematics.keepr.domain.repository.PreferencesRepository
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import kotlinx.coroutines.flow.StateFlow

class AdsCoreRemoteConfigsImpl(
    preferences: PreferencesRepository,
    private val configDataSource: FirebaseConfigDataSource
) : AdsCoreRemoteConfigs {

    override val monetizationEnabledState: StateFlow<Boolean> = preferences.isAdsEnabled

    override val initialRepeatDelayMillis: () -> Long = {
        if (containsKey("initial_repeat_delay_millis")) configDataSource.rawLong("initial_repeat_delay_millis")
        else 10_000
    }
    override val maxRepeatDelayMillis: () -> Long = {
        if (containsKey("max_repeat_delay_millis")) configDataSource.rawLong("max_repeat_delay_millis")
        else 100_000
    }
    override val repeatDelayMultiplier: () -> Double = {
        if (containsKey("repeat_delay_multiplier")) configDataSource.double("repeat_delay_multiplier")
        else
            2.0
    }
    override val isPlacementOffForcefully: (String) -> Boolean = {
        if (containsKey(it)) {
            val value = configDataSource.string(it)
            value.equals("false", ignoreCase = true)
        } else {
            false
        }
    }
    override val monetizationEnabled: () -> Boolean = { monetizationEnabledState.value }
    fun containsKey(key: String): Boolean = configDataSource.containsKey(key)
}
