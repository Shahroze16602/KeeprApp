package com.systematics.monetization.core.utils

sealed interface NativeAdOneTimeUiEvents {

    data object Refresh: NativeAdOneTimeUiEvents
}