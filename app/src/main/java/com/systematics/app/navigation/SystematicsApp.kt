package com.systematics.app.navigation

import androidx.navigation.toRoute
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.systematics.app.domain.model.LaunchDestination
import com.systematics.app.domain.usecase.ResolveLanguageNextDestinationUseCase
import com.systematics.app.domain.usecase.ResolvePremiumNextDestinationUseCase
import com.systematics.app.domain.usecase.ResolveSplashDestinationUseCase
import com.systematics.app.domain.usecase.IsPremium1PurchasedUseCase
import com.systematics.app.domain.usecase.IsBillingEnabledUseCase
import com.systematics.app.presentation.screens.feature.FeatureOneScreen
import com.systematics.app.presentation.screens.feature.FeatureTwoScreen
import com.systematics.app.presentation.screens.language.LanguageScreen
import com.systematics.app.presentation.screens.main.MainScreen
import com.systematics.app.presentation.screens.onboarding.OnboardingScreen
import com.systematics.app.presentation.screens.premium.PremiumScreen
import com.systematics.app.presentation.screens.settings.SettingsScreen
import com.systematics.app.presentation.screens.privacy_policy.PrivacyPolicyScreen
import com.systematics.app.presentation.screens.splash.SplashScreen
import org.koin.compose.koinInject

@Composable
fun SystematicsApp(
    navController: NavHostController,
    resolveSplashDestination: ResolveSplashDestinationUseCase = koinInject(),
    resolvePremiumNextDestination: ResolvePremiumNextDestinationUseCase = koinInject(),
    resolveLanguageNextDestination: ResolveLanguageNextDestinationUseCase = koinInject(),
    isPremium1Purchased: IsPremium1PurchasedUseCase = koinInject(),
    isBillingEnabled: IsBillingEnabledUseCase = koinInject()
) {
    val appDestinations = remember(navController) {
        AppDestinations(navController)
    }
    val startDestination = remember { Routes.SplashScreenRoutes }

    fun navigateTo(destination: LaunchDestination) {
        when (destination) {
            LaunchDestination.PREMIUM -> if (isBillingEnabled()) appDestinations.navigateToPremiumScreen(true) else appDestinations.navigateToMainScreen()
            LaunchDestination.LANGUAGE -> appDestinations.navigateToLanguageScreen(true)
            LaunchDestination.ONBOARDING -> appDestinations.navigateToOnboardingScreen()
            LaunchDestination.HOME -> appDestinations.navigateToMainScreen()
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable<Routes.MainScreenRoutes> {
            MainScreen(
                onFeatureOneClick = appDestinations.navigateToFeatureOneScreen,
                onFeatureTwoClick = appDestinations.navigateToFeatureTwoScreen,
                onSettingsClick = appDestinations.navigateToSettingsScreen,
                isBillingEnabled = isBillingEnabled(),
                onPremiumClick = {
                    when {
                        isBillingEnabled() && !isPremium1Purchased() -> appDestinations.navigateToPremiumScreen(false)
                    }
                }
            )
        }

        composable<Routes.SettingsScreenRoutes> {
            SettingsScreen(onBack = appDestinations.navigateToBackPress)
        }

        composable<Routes.FeatureOneScreenRoutes> {
            FeatureOneScreen(onBack = appDestinations.navigateToBackPress)
        }

        composable<Routes.FeatureTwoScreenRoutes> {
            FeatureTwoScreen(onBack = appDestinations.navigateToBackPress)
        }

        composable<Routes.LanguageScreenRoutes> { backStackEntry ->
            val args = backStackEntry.toRoute<Routes.LanguageScreenRoutes>()
            LanguageScreen(
                isFromSplash = args.isFromSplash,
                onMoveNext = { navigateTo(resolveLanguageNextDestination()) }
            )
        }

        composable<Routes.SplashScreenRoutes> {
            SplashScreen(
                navigateNext = { navigateTo(resolveSplashDestination()) }
            )
        }

        if (isBillingEnabled()) composable<Routes.PremiumScreenRoutes> { backStackEntry ->
            val args = backStackEntry.toRoute<Routes.PremiumScreenRoutes>()
            PremiumScreen(
                isFromSplash = args.isFromSplash,
                isLastPremium = true,
                onNavigateNext = {
                    if (!args.isFromSplash) appDestinations.navigateToBackPress()
                    else navigateTo(resolvePremiumNextDestination())
                },
                onNavigateToPrivacyPolicy = appDestinations.navigateToPrivacyPolicyScreen,
                onNavigateToTermOfUse = appDestinations.navigateToPrivacyPolicyScreen,
            )
        }

        composable<Routes.OnboardingScreenRoutes> {
            OnboardingScreen(
                onNext = { appDestinations.navigateToMainScreen() }
            )
        }

        composable<Routes.PrivacyPolicyScreenRoutes> {
            PrivacyPolicyScreen (
                onBackPress = appDestinations.navigateToBackPress
            )
        }
    }
}
