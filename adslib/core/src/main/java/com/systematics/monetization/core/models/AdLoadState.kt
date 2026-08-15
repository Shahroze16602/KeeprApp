package com.systematics.monetization.core.models

import com.systematics.monetization.core.managers.ad.AdManager

sealed class AdLoadState {
    data object Initialized : AdLoadState()
    data object Loading : AdLoadState()

    data class Loaded(val adManager: AdManager<*>) : AdLoadState()

    data class Failed(val exception: Exception) : AdLoadState()

    data object Shown : AdLoadState()
    data object Destroyed : AdLoadState()
}