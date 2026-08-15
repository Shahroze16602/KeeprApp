package com.systematics.app.presentation.screens.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.monetization.ui.compose.InlineAdShowComponent
import com.systematics.app.R
import com.systematics.app.utils.core.MyColors
import com.systematics.app.domain.usecase.ObserveAppPurchasedUseCase
import com.systematics.app.domain.usecase.IsAdsEnabledUseCase
import com.systematics.app.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.app.presentation.core.utils.FeatureCard
import com.systematics.app.presentation.core.utils.ReusableImage
import com.systematics.app.presentation.core.utils.ReusableText
import com.systematics.app.presentation.core.utils.ReusableTopBar
import com.systematics.app.presentation.core.utils.noRippleClickable
import org.koin.compose.koinInject

@Composable
fun HomeScreen(
    onFeatureOneClick: () -> Unit = {},
    onFeatureTwoClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onPremiumClick: () -> Unit = {},
    observeAppPurchased: ObserveAppPurchasedUseCase = koinInject(),
    isAdsEnabled: IsAdsEnabledUseCase = koinInject(),
    isBillingEnabled: Boolean = true
) {
    val isAppPurchased by observeAppPurchased().collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        ReusableTopBar(
            isBackPressEnable = false,
            titleRes = R.string.app_name,
            onBackClick = {},
            content = {
                if (isBillingEnabled && !isAppPurchased) {
                    ReusableImage(
                        painter = R.drawable.ic_premium,
                        modifier = Modifier
                            .padding(end = 18.dp)
                            .size(24.dp)
                            .noRippleClickable { onPremiumClick() })
                }
                ReusableImage(
                    painter = R.drawable.ic_setting,
                    tintColor = MyColors.BlackColor,
                    modifier = Modifier
                        .size(22.dp)
                        .noRippleClickable { onSettingsClick() })
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp)
        ) {
            ReusableText(
                text = "Welcome 👋",
                fontSize = 22,
                fontWeight = FontWeight.Bold,
                color = MyColors.BlackColor
            )
            ReusableText(
                modifier = Modifier.padding(top = 4.dp),
                text = "Pick a feature to get started.",
                fontSize = 14,
                color = MyColors.TextSecondaryColor
            )

            FeatureCard(
                modifier = Modifier.padding(top = 20.dp),
                badge = "1",
                title = "Feature One",
                subtitle = "Explore the first feature",
                onClick = onFeatureOneClick
            )
            FeatureCard(
                modifier = Modifier.padding(top = 12.dp),
                badge = "2",
                title = "Feature Two",
                subtitle = "Try out the second feature",
                onClick = onFeatureTwoClick
            )
        }

        if (isAdsEnabled()) {
            InlineAdShowComponent(
                modifier = Modifier.fillMaxWidth(), placementKey = AdsPlacement.Inlines.HOME_BOTTOM
            )
        }
    }
}
