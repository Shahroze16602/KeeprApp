package com.systematics.app.presentation.core.utils

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.systematics.app.utils.core.MyColors
import com.systematics.app.utils.core.MyFonts

@Composable
fun ReusableText(
    modifier: Modifier = Modifier,
    text: String,
    fontSize: Int = 14,
    color: Color = MyColors.BlackColor,
    fontWeight: FontWeight = FontWeight.Normal,
    fontFamily: FontFamily = MyFonts.Poppins,
    fontStyle: FontStyle = FontStyle.Normal,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = 2,
    useOverflow: Boolean = false,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textDecoration: TextDecoration? = null,
    letterSpacing: TextUnit = 0.sp,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    Text(
        text = text,
        color = color,
        fontSize = fontSize.sp,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        modifier = modifier,
        textAlign = textAlign,
        fontFamily = fontFamily,
        maxLines = if (useOverflow) maxLines else Int.MAX_VALUE,
        overflow = if (useOverflow) overflow else TextOverflow.Clip,
        textDecoration = textDecoration,
        letterSpacing = letterSpacing,
        lineHeight = lineHeight
    )
}