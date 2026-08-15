package com.systematics.app.presentation.screens.splash

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.compose.FullScreenAdShowComponent
import com.systematics.monetization.ui.compose.utils.monetizationInject
import com.systematics.app.domain.repository.SessionGate
import com.systematics.app.domain.usecase.IsSessionGateAllowedUseCase
import com.systematics.app.domain.usecase.IsShiftSplashAdToPremiumUseCase
import com.systematics.app.domain.usecase.IsAdsEnabledUseCase
import com.systematics.app.utils.monetization.config.backend.AdGroupType
import com.systematics.app.utils.monetization.config.frontend.breakpoints.AdBreakPoint
import com.systematics.app.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.app.utils.providers.LocalAppLogEvents
import com.systematics.app.presentation.screens.splash.events.SplashOneTimeUiEvents
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

private const val TAG = "SplashScreenTAG"

@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(),
    isSessionGateAllowed: IsSessionGateAllowedUseCase = koinInject(),
    isShiftSplashAdToPremium: IsShiftSplashAdToPremiumUseCase = koinInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject(),
    monetizationInstall: MonetizationInstall = monetizationInject(),
    navigateNext: () -> Unit
) {

    val activity = LocalActivity.current
    val appLogEvents = LocalAppLogEvents.current
    val scope = rememberCoroutineScope()

    var showProgress by remember { mutableStateOf(false) }
    var increaseProgress by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val frameDelay = 30L
    val totalFrames = (15 * 1000) / frameDelay
    val increment = 1f / totalFrames

    LaunchedEffect(key1 = increaseProgress) {
        if (increaseProgress) {
            scope.launch {
                while (progress < 1f) {
                    delay(frameDelay)
                    progress += increment
                }
            }
        }
    }

    var showSplashAd by remember {
        mutableStateOf(false)
    }

    var adsLoaded by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(Unit) {
        appLogEvents.loadEvents("splash_launch")
    }

    val loadNextAds = {
        if (isAdsEnabled() && !adsLoaded) {
            if (isSessionGateAllowed(SessionGate.PREMIUM_1)) {
                monetizationInstall.executeBreakPoint(AdBreakPoint.BP_PREMIUM_PENDING)
            } else if (isSessionGateAllowed(SessionGate.LANGUAGE)) {
                monetizationInstall.executeBreakPoint(AdBreakPoint.BP_LANGUAGE_PENDING)
            } else if (isSessionGateAllowed(SessionGate.ONBOARDING)) {
                monetizationInstall.executeBreakPoint(AdBreakPoint.BP_ONBOARDING_PENDING)
            } else {
                monetizationInstall.executeBreakPoint(AdBreakPoint.BP_HOME_PENDING)
            }
            adsLoaded = true
        }
    }

    BackHandler(enabled = true) {
        Log.d(TAG, "SplashScreen: Back press intercepted")
    }

    fun launchMain() {
        increaseProgress = false
        progress = 1f
        loadNextAds()
        navigateNext()
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.initializeSplash(activity!!)
    }

    LaunchedEffect(key1 = Unit) {
        Log.d(TAG, "SplashScreen: actionTest")
        viewModel.oneTimeUiEvents.collect {
            Log.d(TAG, "SplashScreen: actionTest $it")
            when (it) {
                SplashOneTimeUiEvents.NetworkSetupDone -> {
                    if (!isAdsEnabled()) {
                        showProgress = true
                        increaseProgress = true
                    } else if (isShiftSplashAdToPremium()) {
                        monetizationInstall.loadAd(
                            AdGroupType.FullScreenAd.SPLASH_AD,
                            AdsPlacement.FullScreens.SPLASH_WELCOME_AD
                        )
                        increaseProgress = false
                        progress = 1f
                        viewModel.onSplashAdDone(false)
                    } else {
                        showProgress = true
                        increaseProgress = true
                        showSplashAd = true
                    }
                }

                is SplashOneTimeUiEvents.SplashDone -> {
                    launchMain()
                }
            }
        }
    }

    if (isAdsEnabled()) {
        FullScreenAdShowComponent(
            showAd = showSplashAd,
            placementKey = AdsPlacement.FullScreens.SPLASH_WELCOME_AD,
            onAdLoaded = {
                Log.d(TAG, "SplashScreen: ad loaded for splash")
                loadNextAds()
                increaseProgress = false
                progress = 1f
            },
            onDone = {
                Log.d(TAG, "SplashScreen: ad done $it")
                viewModel.onSplashAdDone(it)
            }
        )
    }

    SplashScreenContent(
        showProgress = showProgress,
        progress = progress
    )
}
