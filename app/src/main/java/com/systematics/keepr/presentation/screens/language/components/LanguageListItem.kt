package com.systematics.keepr.presentation.screens.language.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systematics.keepr.utils.core.AppLanguageModel
import com.systematics.keepr.utils.core.MyColors
import com.systematics.keepr.presentation.core.utils.coloredShadow
import com.systematics.keepr.presentation.core.utils.noRippleClickable
import com.systematics.keepr.presentation.screens.premium.components.AppImage
import com.systematics.keepr.presentation.screens.premium.components.AppMainText

@Composable
fun LanguageListItem(
    model: AppLanguageModel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .coloredShadow(
                color = MyColors.BlackColor.copy(alpha = 0.08f),
                cornerRadius = 18.dp,
                blur = 10.dp
            )
            .noRippleClickable(onClick = onClick),
        color = MyColors.WhiteColor,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            width = if (isSelected) 1.dp else 0.8.dp,
            color = if (isSelected) MyColors.AccentColor else MyColors.CardBorderColor
        ),
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AppImage(
                painter = model.languageFlag,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                AppMainText(
                    text = model.languageName,
                    fontSize = 15,
                    fontWeight = FontWeight.SemiBold,
                    color = MyColors.BlackColor,
                    useOverflow = true,
                    maxLines = 1,
                    lineHeight = 18.sp
                )
                AppMainText(
                    text = model.languageDes,
                    fontSize = 12,
                    color = MyColors.TextSecondaryColor,
                    useOverflow = true,
                    maxLines = 1
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = MyColors.AccentColor,
                    unselectedColor = MyColors.TextSecondaryColor
                )
            )
        }
    }
}
