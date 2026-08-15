package com.systematics.app.presentation.screens.premium

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
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.compose.FullScreenAdShowComponent
import com.systematics.monetization.ui.compose.models.AdAction
import com.systematics.monetization.ui.compose.utils.monetizationInject
import com.systematics.monetization.ui.compose.utils.showAd
import com.systematics.monetization.ui.utils.MonetizationSharedState
import com.systematics.app.utils.core.InternetController
import com.systematics.app.domain.usecase.IsShiftSplashAdToPremiumUseCase
import com.systematics.app.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.app.presentation.screens.premium.content.PremiumScreenContent
import com.systematics.app.presentation.screens.premium.event.PremiumEvents
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
    internetController: InternetController = koinInject(),
    monetizationInstall: MonetizationInstall = monetizationInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as Activity

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
    FullScreenAdShowComponent(adAction = adAction.value)

    val proceed: () -> Unit = {
        if (isFromSplash && isLastPremium && isShiftSplashAdToPremium()) {
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
