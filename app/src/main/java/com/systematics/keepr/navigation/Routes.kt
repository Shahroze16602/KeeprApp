package com.systematics.keepr.navigation

import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable data object SplashScreenRoutes : Routes()
    @Serializable data class LanguageScreenRoutes(val isFromSplash: Boolean) : Routes()
    @Serializable data object OnboardingScreenRoutes : Routes()
    @Serializable data object MediaAccessRoutes : Routes()
    @Serializable data object MainScreenRoutes : Routes()
    @Serializable data object SelectedPhotosRoutes : Routes()
    @Serializable data class CleanupSessionRoutes(val scopeKey: String, val title: String, val partial: Boolean = false) : Routes()
    @Serializable data object MediaRecoveryRoutes : Routes()
    @Serializable data object ReviewRoutes : Routes()
    @Serializable data object DeletionConfirmationRoutes : Routes()
    @Serializable data object DeletionProgressRoutes : Routes()
    @Serializable data object CompletionRoutes : Routes()
    @Serializable data object PrivacyPolicyScreenRoutes : Routes()
    @Serializable data object SettingsScreenRoutes : Routes()
    @Serializable data object FeedbackRoutes : Routes()
    @Serializable data object RateUsRoutes : Routes()
    @Serializable data class PermissionDeniedRoutes(val permanentlyDenied: Boolean = false) : Routes()
    @Serializable data object ResumeSessionRoutes : Routes()
    @Serializable data object PartialDeletionRoutes : Routes()
    @Serializable data class PremiumScreenRoutes(val isFromSplash: Boolean) : Routes()
}
