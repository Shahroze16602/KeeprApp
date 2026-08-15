package com.systematics.app.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.compose.InlineAdShowComponent
import com.systematics.monetization.ui.compose.utils.monetizationInject
import com.systematics.app.utils.core.MyColors
import com.systematics.app.domain.usecase.IsAdsEnabledUseCase
import com.systematics.app.utils.monetization.config.frontend.breakpoints.AdBreakPoint
import com.systematics.app.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.app.presentation.core.utils.AppLogoBadge
import com.systematics.app.presentation.core.utils.InfoRow
import com.systematics.app.presentation.core.utils.PrimaryActionButton
import com.systematics.app.presentation.core.utils.ReusableText
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun OnboardingScreen(
    onNext: () -> Unit,
    monetizationInstall: MonetizationInstall = monetizationInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject()
) {
    var preloaded by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100.milliseconds)
        if (isAdsEnabled() && !preloaded) {
            monetizationInstall.executeBreakPoint(AdBreakPoint.BP_HOME_PENDING)
            preloaded = true
        }
    }

    Scaffold(containerColor = MyColors.BackgroundColor) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppLogoBadge(modifier = Modifier.padding(top = 32.dp), size = 88)

                ReusableText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    text = "Welcome aboard",
                    fontSize = 24,
                    fontWeight = FontWeight.Bold,
                    color = MyColors.BlackColor,
                    textAlign = TextAlign.Center
                )
                ReusableText(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    text = "A quick look at what you can do.",
                    fontSize = 14,
                    color = MyColors.TextSecondaryColor,
                    textAlign = TextAlign.Center
                )

                Column(modifier = Modifier.padding(top = 28.dp)) {
                    InfoRow("✦", "Simple & fast", "Everything just a tap away")
                    InfoRow("◆", "Powerful features", "Tools that get the job done")
                    InfoRow("●", "Always free to start", "Upgrade only if you want more")
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MyColors.BackgroundColor)
                    .padding(horizontal = 24.dp)
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                PrimaryActionButton(text = "Continue", onClick = onNext)
            }

            if (isAdsEnabled()) {
                InlineAdShowComponent(
                    modifier = Modifier.fillMaxWidth(),
                    placementKey = AdsPlacement.Inlines.ONBOARDING_BOTTOM
                )
            }
        }
    }
}
