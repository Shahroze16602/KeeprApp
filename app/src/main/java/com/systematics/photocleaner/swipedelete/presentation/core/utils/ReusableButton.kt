package com.systematics.photocleaner.swipedelete.presentation.core.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.systematics.photocleaner.swipedelete.utils.core.MyColors

@Composable
fun ReusableButton(
    modifier: Modifier = Modifier,
    text: Int,
    onClick: () -> Unit,
    enabled: Boolean = true,
    height: Int = 48,
    radius: Int = 28,
    maxLines: Int = 1,
    overflow: Boolean = true,
    showShadow: Boolean = enabled,
    backgroundColor: Color = MyColors.BlackColor,
    textColor: Color = MyColors.WhiteColor,
    fontWeight: FontWeight = FontWeight.SemiBold,
    disabledBackgroundColor: Color = MyColors.BlackColor,
    disabledTextColor: Color = MyColors.WhiteColor,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor,
            disabledContainerColor = disabledBackgroundColor,
            disabledContentColor = disabledTextColor
        ),
        shape = RoundedCornerShape(radius.dp),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp)
            .run {
                if (showShadow) {
                    coloredShadow(
                        color = MyColors.BlackColor,
                        offsetY = 2.dp,
                        blur = 6.dp,
                        cornerRadius = radius.dp
                    )
                } else {
                    this
                }
            }
    ) {
        Row(
            horizontalArrangement = if (leadingIcon != null) Arrangement.SpaceBetween else Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                HorizontalSpacer(8)
            }
            ReusableText(
                modifier = Modifier.wrapContentWidth(),
                text = stringResource(text),
                fontWeight = fontWeight,
                fontSize = 16,
                textAlign = TextAlign.Center,
                color = if (enabled) textColor else disabledTextColor,
                maxLines = maxLines,
                overflow = if (overflow) TextOverflow.Ellipsis else TextOverflow.Clip
            )
            if (trailingIcon != null) {
                HorizontalSpacer(8)
                trailingIcon()
            }
        }
    }
}