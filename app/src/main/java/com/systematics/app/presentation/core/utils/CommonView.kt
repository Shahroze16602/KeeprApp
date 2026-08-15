package com.systematics.app.presentation.core.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Indication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativePaint
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.systematics.app.utils.core.MyColors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HorizontalSpacer(size: Int = 10) {
    Spacer(modifier = Modifier.width(size.dp))
}

@Composable
fun VerticalSpacer(size: Int = 10) {
    Spacer(modifier = Modifier.height(size.dp))
}

fun Modifier.coloredShadow(
    color: Color,
    cornerRadius: Dp,
    blur: Dp = 8.dp,
    offsetY: Dp = 0.5.dp,
    offsetX: Dp = 0.dp,
): Modifier = this.drawBehind {
    val blurPx = blur.toPx()
    val offsetXPx = offsetX.toPx()
    val offsetYPx = offsetY.toPx()
    val cornerPx = cornerRadius.toPx()

    val paint = Paint()
    paint.nativePaint.apply {
        isAntiAlias = true
        this.color = android.graphics.Color.TRANSPARENT
        setShadowLayer(blurPx, offsetXPx, offsetYPx, color.toArgb())
    }

    drawIntoCanvas {
        it.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            cornerPx,
            cornerPx,
            paint = paint
        )
    }
}

fun Modifier.noRippleClickable(
    waitedDelay: Long = 400L,
    enabled: Boolean = true,
    onClick: () -> Unit,
): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    var clickJob: Job? by remember { mutableStateOf(null) }

    safeClickAbleNoRipple(
        enabled = enabled,
        indication = null,
        interactionSource = interaction
    ) {
        if (clickJob?.isActive == true) return@safeClickAbleNoRipple
        clickJob = scope.launch {
            onClick()
            delay(waitedDelay)
        }
    }
}

fun Modifier.safeClickAbleNoRipple(
    enabled: Boolean = true,
    indication: Indication? = null,
    interactionSource: MutableInteractionSource? = null,
    onClick: () -> Unit,
): Modifier = composed {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    this.then(
        Modifier.clickable(
            enabled = enabled,
            interactionSource = source,
            indication = indication,
            onClick = onClick
        )
    )
}


@Composable
fun LottieIconAnimation(
    modifier: Modifier = Modifier,
    lottie: Int,
    isOneTimeAnimate: Boolean = false,
    contentScale: ContentScale = ContentScale.Fit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(lottie))
    val progress by animateLottieCompositionAsState(
        composition, iterations = if (isOneTimeAnimate) 1 else LottieConstants.IterateForever
    )
    LottieAnimation(
        modifier = modifier,
        contentScale = contentScale,
        composition = composition,
        progress = { progress },
    )
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MyColors.BlackColor)
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}