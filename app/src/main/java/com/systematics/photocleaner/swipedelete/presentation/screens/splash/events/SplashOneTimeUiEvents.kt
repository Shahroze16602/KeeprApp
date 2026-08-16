package com.systematics.photocleaner.swipedelete.presentation.screens.splash.events

sealed interface SplashOneTimeUiEvents {

    data object NetworkSetupDone : SplashOneTimeUiEvents
    data class SplashDone(val adShown: Boolean) : SplashOneTimeUiEvents
}