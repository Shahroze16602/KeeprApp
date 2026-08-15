package com.systematics.app.presentation.screens.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.systematics.app.R
import com.systematics.app.utils.core.MyColors
import com.systematics.app.presentation.core.utils.ReusableImage
import com.systematics.app.presentation.core.utils.ReusableText
import com.systematics.app.presentation.core.utils.VerticalSpacer

@Preview
@Composable
fun SplashScreenContent(
    showProgress: Boolean = true, progress: Float = 0f
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ReusableImage(
                painter = R.drawable.ic_app_logo,
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
            VerticalSpacer(15)
            ReusableText(
                text = stringResource(R.string.app_name),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
        Box(
            modifier = Modifier
                .padding(bottom = 40.dp, start = 70.dp, end = 70.dp)
                .fillMaxWidth()
                .height(8.dp)
        ) {
            if (showProgress) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    gapSize = 2.dp,
                    progress = { progress },
                    color = MyColors.BlackColor,
                    drawStopIndicator = {},
                    trackColor = MyColors.WhiteColor
                )
            }
        }
    }
}
