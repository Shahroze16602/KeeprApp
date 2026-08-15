package com.systematics.monetization.core.models

import com.systematics.monetization.core.managers.ad.AdManager

sealed class AdsNetworkState {
    data object Created : AdsNetworkState()
    data object Initialized : AdsNetworkState()

    data class Paused(val adManager: AdManager<*>) : AdsNetworkState()

    data class Failed(val exception: Exception) : AdsNetworkState()

    data object Deffered : AdsNetworkState()
}