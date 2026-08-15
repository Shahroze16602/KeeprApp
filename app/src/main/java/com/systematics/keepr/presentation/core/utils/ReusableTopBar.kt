package com.systematics.keepr.presentation.core.utils

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.systematics.keepr.R
import com.systematics.keepr.utils.core.MyColors

@Composable
fun ReusableTopBar(
    titleRes: Int = -1,
    isBackPressEnable: Boolean = true,
    onBackClick: () -> Unit,
    content: @Composable RowScope.() -> Unit = {},
) {
    CoreTopBar(mainContent = {
        if (isBackPressEnable) {
            ReusableImage(
                painter = R.drawable.ic_back_press,
                tintColor = MyColors.BlackColor,
                modifier = Modifier
                    .size(14.dp)
                    .noRippleClickable(
                        onClick = onBackClick
                    )
            )
        }
        if (titleRes != -1) {
            ReusableText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (isBackPressEnable) 20.dp else 0.dp)
                    .weight(1f),
                text = stringResource(titleRes),
                fontWeight = FontWeight.Bold,
                fontSize = 18,
                color = MyColors.BlackColor,
                textAlign = TextAlign.Start
            )
        }
    }, content = content)
}

@Composable
fun CoreTopBar(
    mainContent: @Composable RowScope.() -> Unit,
    content: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            content = mainContent
        )
        Row(
            verticalAlignment = Alignment.CenterVertically, content = content
        )
    }
}
