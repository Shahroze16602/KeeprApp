package com.systematics.photocleaner.swipedelete.presentation.screens.premium.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun VerticalSpace(dp: Dp = 10.dp) {
    Spacer(modifier = Modifier.height(dp))
}

@Composable
fun HorizontalSpace(dp: Dp = 10.dp) {
    Spacer(modifier = Modifier.width(dp))
}

@Composable
fun PrimaryButtonRounded(
    modifier: Modifier = Modifier,
    horizontalPadding: Int = 40,
    paddingValues: PaddingValues = PaddingValues(horizontal = 15.dp, vertical = 10.dp),
    gradientColors: List<Color> = listOf(Color(0xFF825FFE), Color(0xFF825FFE)),
    transparentColors: List<Color> = listOf(Color.Transparent, Color.Transparent),
    waitedDelay: Long = 2000,
    onButtonClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding.dp)
            .background(Brush.linearGradient(gradientColors), RoundedCornerShape(10.dp))
            .border(
                width = 1.dp, brush = Brush.linearGradient(transparentColors), shape = CircleShape
            )
            .padding(paddingValues)
            .clickableNoRipple(waitedDelay = waitedDelay) {
                onButtonClick.invoke()

            }, contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun Modifier.clickableNoRipple(
    waitedDelay: Long = 2000, onClick: () -> Unit
): Modifier {
    val coroutineScope = rememberCoroutineScope()
    var clickJob: Job? = null
    return this.then(
        Modifier.clickable(
        indication = null, interactionSource = remember { MutableInteractionSource() }) {
        if (clickJob == null) {
            clickJob = coroutineScope.launch {
                onClick.invoke()
                delay(waitedDelay)
                clickJob = null
            }
        }
    })
}

@Composable
fun AppMainText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = Color.Black,
    fontSize: Int = 14,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = 2,
    useOverflow: Boolean = false,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textDecoration: TextDecoration? = null,
    lineHeight: TextUnit? = null,
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        modifier = modifier,
        textAlign = textAlign,
        maxLines = if (useOverflow) maxLines else Int.MAX_VALUE,
        overflow = if (useOverflow) overflow else TextOverflow.Clip,
        textDecoration = textDecoration,
        lineHeight = lineHeight ?: TextUnit.Unspecified
    )
}

@Composable
fun AppImage(
    @DrawableRes painter: Int,
    modifier: Modifier = Modifier.size(20.dp),
    tintColor: Color? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
) {
    Image(
        painter = painterResource(painter),
        contentDescription = "ImageHere",
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        colorFilter = tintColor?.let { ColorFilter.tint(it) } ?: run { null })
}
