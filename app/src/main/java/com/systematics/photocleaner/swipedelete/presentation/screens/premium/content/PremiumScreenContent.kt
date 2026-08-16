package com.systematics.photocleaner.swipedelete.presentation.screens.premium.content

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.systematics.monetization.ui.compose.InlineAdShowComponent
import com.systematics.photocleaner.swipedelete.R
import com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.config.AdsPlacement
import com.systematics.photocleaner.swipedelete.presentation.screens.premium.components.AppImage
import com.systematics.photocleaner.swipedelete.presentation.screens.premium.components.AppMainText
import com.systematics.photocleaner.swipedelete.presentation.screens.premium.components.PrimaryButtonRounded
import com.systematics.photocleaner.swipedelete.presentation.screens.premium.components.VerticalSpace
import com.systematics.photocleaner.swipedelete.presentation.screens.premium.components.clickableNoRipple
import com.systematics.photocleaner.swipedelete.presentation.screens.premium.state.PremiumUiState

private val AccentStart = Color(0xFF9B7BFF)
private val AccentEnd = Color(0xFF6A43F0)
private val TextSecondary = Color(0xFF6B6B6B)

@Composable
fun PremiumScreenContent(
    state: PremiumUiState,
    isCloseVisible: Boolean = true,
    onPurchaseClick: () -> Unit,
    onCancelClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onTermOfUseClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .statusBarsPadding()
            .background(White)
    ) {
        InlineAdShowComponent(
            modifier = Modifier.fillMaxWidth(),
            placementKey = AdsPlacement.Inlines.PREMIUM_TOP
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            CloseButton(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .alpha(if (isCloseVisible) 1f else 0f),
                onClick = { if (isCloseVisible) onCancelClick() }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppImage(
                painter = R.drawable.ic_app_logo,
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
            VerticalSpace(18.dp)
            AppMainText(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.app_name),
                fontSize = 22,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            VerticalSpace(8.dp)
            AppMainText(
                modifier = Modifier.fillMaxWidth(),
                text = "Welcome aboard! Tap continue to get started.",
                color = TextSecondary,
                fontSize = 14,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
        ) {
            when {
                state.isLoading -> {
                    PrimaryButtonRounded(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        horizontalPadding = 0,
                        gradientColors = listOf(AccentStart, AccentEnd),
                        onButtonClick = {}
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    }
                }

                else -> {
                    PrimaryButtonRounded(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        horizontalPadding = 0,
                        gradientColors = listOf(AccentStart, AccentEnd),
                        onButtonClick = { onPurchaseClick() }
                    ) {
                        AppMainText(
                            text = "Continue",
                            color = White,
                            fontSize = 17,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            useOverflow = true,
                        )
                    }
                }
            }

            VerticalSpace(14.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppMainText(
                    text = "Terms of Service",
                    color = TextSecondary,
                    fontSize = 11,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    useOverflow = true,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clickableNoRipple { onTermOfUseClick() }
                )
                VerticalDivider(
                    modifier = Modifier.height(12.dp), color = Color(0xFFBFBFBF)
                )
                AppMainText(
                    text = "Privacy Policy",
                    color = TextSecondary,
                    fontSize = 11,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    useOverflow = true,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                        .clickableNoRipple { onPrivacyPolicyClick() }
                )
            }
        }
    }
}

@Composable
private fun CloseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFFF1F1F1))
            .clickableNoRipple { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(13.dp)) {
            val stroke = 2.dp.toPx()
            drawLine(
                color = Color(0xFF333333),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF333333),
                start = Offset(size.width, 0f),
                end = Offset(0f, size.height),
                strokeWidth = stroke,
                cap = StrokeCap.Round
            )
        }
    }
}
