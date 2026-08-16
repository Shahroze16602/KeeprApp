package com.systematics.photocleaner.swipedelete.presentation.screens.premium

import android.app.Activity
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.monetization.ui.compose.FullScreenAdShowComponent
import com.systematics.monetization.ui.compose.models.AdAction
import com.systematics.monetization.ui.compose.utils.showAd
import com.systematics.monetization.ui.utils.MonetizationSharedState
import com.systematics.photocleaner.swipedelete.utils.core.InternetController
import com.systematics.photocleaner.swipedelete.domain.usecase.IsShiftSplashAdToPremiumUseCase
import com.systematics.photocleaner.swipedelete.domain.usecase.IsAdsEnabledUseCase
import com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.photocleaner.swipedelete.presentation.screens.premium.content.PremiumScreenContent
import com.systematics.photocleaner.swipedelete.presentation.screens.premium.event.PremiumEvents
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun PremiumScreen(
    isFromSplash: Boolean,
    isLastPremium: Boolean = true,
    onNavigateNext: () -> Unit,
    onNavigateToPrivacyPolicy: () -> Unit,
    onNavigateToTermOfUse: () -> Unit,
    viewModel: PremiumViewModel = koinViewModel(),
    isShiftSplashAdToPremium: IsShiftSplashAdToPremiumUseCase = koinInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject(),
    internetController: InternetController = koinInject(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity
    val adsEnabled = isAdsEnabled()

    DisposableEffect(Unit) {
        MonetizationSharedState.blockAppOpen()
        onDispose { MonetizationSharedState.unblockAppOpen() }
    }

    var navigated by rememberSaveable { mutableStateOf(false) }
    val goNext: () -> Unit = {
        if (!navigated) {
            navigated = true
            onNavigateNext()
        }
    }

    val adAction = remember { mutableStateOf<AdAction?>(null) }
    if (adsEnabled) FullScreenAdShowComponent(adAction = adAction.value)

    val proceed: () -> Unit = {
        if (adsEnabled && isFromSplash && isLastPremium && isShiftSplashAdToPremium()) {
            adAction.showAd(AdsPlacement.FullScreens.SPLASH_ON_PREMIUM_CLOSE) { goNext() }
        } else {
            goNext()
        }
    }

    val onPurchaseClick: () -> Unit = {
        if (state.selectedOffer == null && !internetController.isInternetConnected) {
            Toast.makeText(context, "No internet connection", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.onEvent(
                events = PremiumEvents.PremiumBuyRequested(
                    activity = activity,
                    onDone = { success -> if (success) goNext() }
                )
            )
        }
    }

    PremiumScreenContent(
        state = state,
        onPurchaseClick = onPurchaseClick,
        onCancelClick = proceed,
        onPrivacyPolicyClick = onNavigateToPrivacyPolicy,
        onTermOfUseClick = onNavigateToTermOfUse
    )
}
