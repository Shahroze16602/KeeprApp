package com.systematics.app.presentation.screens.feature

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.systematics.monetization.ui.compose.FullScreenAdShowComponent
import com.systematics.monetization.ui.compose.InlineAdShowComponent
import com.systematics.monetization.ui.compose.models.AdAction
import com.systematics.monetization.ui.compose.utils.showAd
import com.systematics.app.utils.core.MyColors
import com.systematics.app.domain.usecase.IsAdsEnabledUseCase
import com.systematics.app.presentation.core.utils.AppLogoBadge
import com.systematics.app.presentation.core.utils.PrimaryActionButton
import com.systematics.app.presentation.core.utils.ReusableText
import org.koin.compose.koinInject

@Composable
fun FeatureScreen(
    featureName: String,
    bottomPlacement: String,
    backPlacement: String,
    actionPlacement: String,
    onBack: () -> Unit,
    isAdsEnabled: IsAdsEnabledUseCase = koinInject()
) {
    val adAction = remember { mutableStateOf<AdAction?>(null) }
    if (isAdsEnabled()) {
        FullScreenAdShowComponent(adAction = adAction.value)
    }

    val backWithAd: () -> Unit = {
        if (isAdsEnabled()) adAction.showAd(backPlacement) { onBack() } else onBack()
    }

    val runFeatureWithAd: () -> Unit = {
        if (isAdsEnabled()) adAction.showAd(actionPlacement) {}
    }

    BackHandler { backWithAd() }

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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                AppLogoBadge(size = 76)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MyColors.WhiteColor)
                        .border(1.dp, MyColors.CardBorderColor, RoundedCornerShape(18.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ReusableText(
                        modifier = Modifier.fillMaxWidth(),
                        text = featureName,
                        fontSize = 22,
                        fontWeight = FontWeight.Bold,
                        color = MyColors.BlackColor,
                        textAlign = TextAlign.Center
                    )
                    ReusableText(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        text = "This is a dummy screen mimicking the $featureName feature.",
                        fontSize = 14,
                        color = MyColors.TextSecondaryColor,
                        textAlign = TextAlign.Center
                    )
                }

                PrimaryActionButton(
                    modifier = Modifier.padding(top = 28.dp),
                    text = "Use $featureName",
                    onClick = runFeatureWithAd
                )

                ReusableText(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable { backWithAd() },
                    text = "Go Back",
                    fontSize = 15,
                    fontWeight = FontWeight.SemiBold,
                    color = MyColors.AccentColor
                )
            }

            if (isAdsEnabled()) {
                InlineAdShowComponent(
                    modifier = Modifier.fillMaxWidth(),
                    placementKey = bottomPlacement
                )
            }
        }
    }
}
