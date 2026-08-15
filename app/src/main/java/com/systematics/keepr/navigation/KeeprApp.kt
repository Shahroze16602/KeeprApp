package com.systematics.keepr.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.systematics.keepr.data.keepr.KeeprController
import com.systematics.keepr.domain.model.LaunchDestination
import com.systematics.keepr.domain.usecase.*
import com.systematics.keepr.presentation.keepr.*
import com.systematics.keepr.presentation.screens.premium.PremiumScreen
import com.systematics.keepr.presentation.screens.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun KeeprApp(
    navController: NavHostController,
    resolveSplashDestination: ResolveSplashDestinationUseCase = koinInject(),
    resolvePremiumNextDestination: ResolvePremiumNextDestinationUseCase = koinInject(),
    resolveLanguageNextDestination: ResolveLanguageNextDestinationUseCase = koinInject(),
    isPremium1Purchased: IsPremium1PurchasedUseCase = koinInject(),
    isBillingEnabled: IsBillingEnabledUseCase = koinInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject(),
    controller: KeeprController = koinInject(),
) {
    val destinations = remember(navController) { AppDestinations(navController) }
    val appState by controller.state.collectAsStateWithLifecycle()

    fun navigateTo(destination: LaunchDestination) {
        when (destination) {
            LaunchDestination.PREMIUM -> if (isBillingEnabled()) destinations.premium(true) else destinations.main()
            LaunchDestination.LANGUAGE -> destinations.language(true)
            LaunchDestination.ONBOARDING -> {
                if (controller.hasFullAccess() || controller.hasPartialAccess()) destinations.main()
                else destinations.onboarding()
            }
            LaunchDestination.HOME -> destinations.main()
        }
    }

    KeeprTheme(appState.darkMode, !appState.fullMotion, appState.haptics) {
        NavHost(navController, Routes.SplashScreenRoutes) {
            composable<Routes.SplashScreenRoutes> {
                SplashScreen(navigateNext = { navigateTo(resolveSplashDestination()) })
            }
            composable<Routes.LanguageScreenRoutes> { entry ->
                val args = entry.toRoute<Routes.LanguageScreenRoutes>()
                KeeprLanguageScreen(
                    isFromSplash = args.isFromSplash,
                    onBack = destinations.back,
                    onContinue = { if (args.isFromSplash) navigateTo(resolveLanguageNextDestination()) else destinations.back() }
                )
            }
            composable<Routes.OnboardingScreenRoutes> {
                KeeprOnboardingScreen(onContinue = destinations.mediaAccess, onPrivacy = destinations.privacy)
            }
            composable<Routes.MediaAccessRoutes> {
                MediaAccessScreen(onGranted = destinations.main, onSelected = destinations.selected, onDenied = destinations.denied, onNotNow = destinations.main)
            }
            composable<Routes.MainScreenRoutes> {
                MonthPickerScreen(
                    onSettings = destinations.settings,
                    showPremium = isBillingEnabled() && isAdsEnabled(),
                    onPremium = { if (isBillingEnabled() && isAdsEnabled()) destinations.premium(false) },
                    onStart = { key, title, partial -> controller.startMonth(key, title, partial); destinations.session(key, title, partial) },
                    onResume = { key, title -> controller.startMonth(key, title); destinations.resume() },
                    onPermission = destinations.mediaAccess,
                    onEmpty = { destinations.empty(null) },
                    onSelected = destinations.selected,
                )
            }
            composable<Routes.SelectedPhotosRoutes> {
                SelectedPhotosScreen(onBack = destinations.main, onAccess = destinations.mediaAccess,
                    onStart = { controller.startMonth("selected", "Selected", true); destinations.session("selected", "Selected", true) })
            }
            composable<Routes.CleanupSessionRoutes> { entry ->
                val args = entry.toRoute<Routes.CleanupSessionRoutes>()
                LaunchedEffect(args.scopeKey) {
                    if (controller.state.value.session?.scopeKey != args.scopeKey) controller.startMonth(args.scopeKey, args.title, args.partial)
                }
                CleanupSessionScreen(
                    onExit = destinations.main,
                    onReview = destinations.review,
                    onRecovery = destinations.recovery,
                    expectedScopeKey = args.scopeKey,
                )
            }
            composable<Routes.MediaRecoveryRoutes> { MediaRecoveryScreen(onBack = destinations.back, onRetry = destinations.back, onSkip = { controller.skipUnavailable(); destinations.back() }) }
            composable<Routes.ReviewRoutes> { ReviewScreen(onBack = destinations.back, onSave = destinations.main, onConfirm = destinations.confirm, onFinish = { controller.finishWithoutDeletion(); destinations.completion() }) }
            composable<Routes.DeletionConfirmationRoutes> { DeletionConfirmationScreen(onBack = destinations.back, onConfirm = { controller.beginDeletion(); destinations.deletion() }) }
            composable<Routes.DeletionProgressRoutes> { DeletionProgressScreen(onComplete = destinations.completion, onPartial = destinations.partial, onLater = destinations.main) }
            composable<Routes.CompletionRoutes> { CompletionScreen(onMonths = destinations.main, onRate = destinations.rate, onPartial = destinations.partial) }
            composable<Routes.PrivacyPolicyScreenRoutes> { KeeprPrivacyScreen(onBack = destinations.back) }
            composable<Routes.SettingsScreenRoutes> {
                KeeprSettingsScreen(onBack = destinations.back, onLanguage = { destinations.language(false) }, onAccess = destinations.mediaAccess,
                    onAnalytics = destinations.analytics, onPrivacy = destinations.privacy, onFeedback = destinations.feedback,
                    onRate = destinations.rate, onReplayIntro = destinations.onboarding, onReset = destinations.reset)
            }
            composable<Routes.AnalyticsConsentRoutes> { AnalyticsConsentScreen(onBack = destinations.back) }
            composable<Routes.FeedbackRoutes> { FeedbackScreen(onBack = destinations.back) }
            composable<Routes.RateUsRoutes> { RateUsScreen(onBack = destinations.back, onFeedback = destinations.feedback) }
            composable<Routes.ResetKeeprRoutes> { ResetKeeprScreen(onCancel = destinations.back, onReset = { controller.resetKeepr(); navController.navigate(Routes.SplashScreenRoutes) { popUpTo(0) } }) }
            composable<Routes.PermissionDeniedRoutes> { entry ->
                val args = entry.toRoute<Routes.PermissionDeniedRoutes>()
                PermissionDeniedScreen(args.permanentlyDenied, destinations.mediaAccess, destinations.selected, destinations.privacy, destinations.main)
            }
            composable<Routes.EmptyLibraryRoutes> { entry ->
                EmptyLibraryScreen(entry.toRoute<Routes.EmptyLibraryRoutes>().monthTitle, destinations.main, controller::refreshCatalog, destinations.mediaAccess, destinations.settings)
            }
            composable<Routes.ResumeSessionRoutes> { ResumeSessionScreen(onResume = { destinations.session(appState.session?.scopeKey ?: "", appState.session?.title ?: "", appState.session?.partial ?: false) }, onRestart = { controller.restartSession(); destinations.session(appState.session?.scopeKey ?: "", appState.session?.title ?: "", appState.session?.partial ?: false) }, onMonths = destinations.main, onReview = destinations.review) }
            composable<Routes.PartialDeletionRoutes> { PartialDeletionScreen(onReview = destinations.review, onRetry = { controller.beginDeletion(); destinations.deletion() }, onMonths = destinations.main) }
            if (isBillingEnabled()) composable<Routes.PremiumScreenRoutes> { entry ->
                val args = entry.toRoute<Routes.PremiumScreenRoutes>()
                PremiumScreen(
                    isFromSplash = args.isFromSplash, isLastPremium = true,
                    onNavigateNext = { if (args.isFromSplash) navigateTo(resolvePremiumNextDestination()) else destinations.back() },
                    onNavigateToPrivacyPolicy = destinations.privacy, onNavigateToTermOfUse = destinations.privacy,
                )
            }
        }
    }
}
