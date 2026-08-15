package com.systematics.app.navigation

import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    data object MainScreenRoutes : Routes()

    @Serializable
    data class LanguageScreenRoutes(
        val isFromSplash: Boolean
    ) : Routes()

    @Serializable
    data object SplashScreenRoutes : Routes()

    @Serializable
    data class PremiumScreenRoutes(
        val isFromSplash: Boolean
    ) : Routes()

    @Serializable
    data object OnboardingScreenRoutes : Routes()

    @Serializable
    data object SettingsScreenRoutes : Routes()

    @Serializable
    data object FeatureOneScreenRoutes : Routes()

    @Serializable
    data object FeatureTwoScreenRoutes : Routes()

    @Serializable
    data object PrivacyPolicyScreenRoutes : Routes()
}
