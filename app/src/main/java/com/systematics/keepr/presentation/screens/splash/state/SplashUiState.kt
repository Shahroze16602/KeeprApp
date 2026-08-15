package com.systematics.keepr.presentation.screens.splash.state

data class SplashUiState(
    val initialAdsLoading: Boolean = false,
    val splashLoadingState: SplashLoadingState = SplashLoadingState.Idle
)

sealed interface SplashLoadingState {

    data object Idle : SplashLoadingState
    data object NetworkSetupDone : SplashLoadingState
    data object AdWaiting : SplashLoadingState
    data object AdBlocked : SplashLoadingState
    data object Done : SplashLoadingState
}