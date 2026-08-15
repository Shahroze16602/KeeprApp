package com.systematics.app.navigation

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import com.systematics.app.utils.core.firstEntry
import com.systematics.app.utils.core.lastEntry

class AppDestinations(
    private val navController: NavController
) {
    val navigateToBackPress: () -> Unit = {
        if (navController.previousBackStackEntry != null) {
            navController.popBackStack()
        }
    }

    val navigateToMainScreen = {
        val popped = navController.popBackStack(Routes.MainScreenRoutes, inclusive = false)
        if (!popped) {
            navigateClearingAll(Routes.MainScreenRoutes)
        }
    }

    val navigateToLanguageScreen: (Boolean) -> Unit = { isFromSplash ->
        navigatePoppingLast(Routes.LanguageScreenRoutes(isFromSplash))
    }

    var navigateToPremiumScreen: (Boolean) -> Unit = { isFromSplash ->
        if (isFromSplash) {
            navigatePoppingLast(Routes.PremiumScreenRoutes(isFromSplash))
        } else {
            navController.navigateSafely(Routes.PremiumScreenRoutes(isFromSplash))
        }
    }

    var navigateToOnboardingScreen: () -> Unit = {
        navigatePoppingLast(Routes.OnboardingScreenRoutes)
    }

    val navigateToSettingsScreen: () -> Unit = {
        navController.navigateSafely(Routes.SettingsScreenRoutes)
    }

    val navigateToFeatureOneScreen: () -> Unit = {
        navController.navigateSafely(Routes.FeatureOneScreenRoutes)
    }

    val navigateToFeatureTwoScreen: () -> Unit = {
        navController.navigateSafely(Routes.FeatureTwoScreenRoutes)
    }

    var navigateToPrivacyPolicyScreen: () -> Unit = {
        navController.navigateSafely(Routes.PrivacyPolicyScreenRoutes)
    }

    private fun NavController.navigateSafely(route: Any) {
        if (currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            navigate(route)
        }
    }

    private fun navigatePoppingLast(route: Routes, include: Boolean = true) {
        navController.navigate(route) {
            navController.visibleEntries.value.lastEntry()?.destination?.route?.let {
                Log.d(TAG, "navigatePoppingLast: popping up to $it while navigating to $route")
                popUpTo(it) {
                    inclusive = include
                }
            }
        }
    }

    private fun navigateClearingAll(route: Routes) {
        navController.navigate(route) {
            navController.visibleEntries.value.firstEntry()?.destination?.route?.let {
                Log.d(TAG, "navigateClearingAll: popping up to $it while navigating to $route")
                popUpTo(it) {
                    inclusive = true
                }
            }
        }
    }

    companion object {
        private const val TAG = "AppDestinationsTAG"
    }
}
