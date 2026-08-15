package com.systematics.keepr.presentation.core.utils

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

@Composable
fun ReusableImage(
    modifier: Modifier = Modifier,
    @DrawableRes painter: Int,
    tintColor: Color? = null,
    contentScale: ContentScale = ContentScale.Fit,
) {
    Image(
        modifier = modifier,
        painter = painterResource(painter),
        contentDescription = "ImageHere",
        contentScale = contentScale,
        colorFilter = tintColor?.let { ColorFilter.tint(it) } ?: run { null })
}