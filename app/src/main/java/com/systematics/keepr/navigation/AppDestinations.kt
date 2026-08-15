package com.systematics.keepr.navigation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController

class AppDestinations(private val navController: NavController) {
    val back: () -> Unit = { if (navController.previousBackStackEntry != null) navController.popBackStack() }
    val main: () -> Unit = { navController.navigate(Routes.MainScreenRoutes) { popUpTo(0) } }
    val settings = { navController.safe(Routes.SettingsScreenRoutes) }
    val premium: (Boolean) -> Unit = { navController.safe(Routes.PremiumScreenRoutes(it)) }
    val language: (Boolean) -> Unit = { navController.safe(Routes.LanguageScreenRoutes(it)) }
    val onboarding = { navController.safe(Routes.OnboardingScreenRoutes) }
    val mediaAccess = { navController.safe(Routes.MediaAccessRoutes) }
    val selected = { navController.safe(Routes.SelectedPhotosRoutes) }
    val session: (String, String, Boolean) -> Unit = { key, title, partial -> navController.safe(Routes.CleanupSessionRoutes(key, title, partial)) }
    val recovery = { navController.safe(Routes.MediaRecoveryRoutes) }
    val review = { navController.safe(Routes.ReviewRoutes) }
    val confirm = { navController.safe(Routes.DeletionConfirmationRoutes) }
    val deletion = { navController.safe(Routes.DeletionProgressRoutes) }
    val completion = { navController.safe(Routes.CompletionRoutes) }
    val privacy = { navController.safe(Routes.PrivacyPolicyScreenRoutes) }
    val analytics = { navController.safe(Routes.AnalyticsConsentRoutes) }
    val feedback = { navController.safe(Routes.FeedbackRoutes) }
    val rate = { navController.safe(Routes.RateUsRoutes) }
    val reset = { navController.safe(Routes.ResetKeeprRoutes) }
    val denied: (Boolean) -> Unit = { navController.safe(Routes.PermissionDeniedRoutes(it)) }
    val empty: (String?) -> Unit = { navController.safe(Routes.EmptyLibraryRoutes(it)) }
    val resume = { navController.safe(Routes.ResumeSessionRoutes) }
    val partial = { navController.safe(Routes.PartialDeletionRoutes) }
    private fun NavController.safe(route: Any) {
        if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navigate(route)
    }
}
