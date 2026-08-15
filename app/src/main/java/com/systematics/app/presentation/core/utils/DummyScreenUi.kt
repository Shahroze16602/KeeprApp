package com.systematics.app.presentation.core.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.systematics.app.R
import com.systematics.app.utils.core.MyColors

@Composable
fun AppLogoBadge(
    modifier: Modifier = Modifier,
    size: Int = 88
) {
    ReusableImage(
        painter = R.drawable.ic_app_logo,
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(22.dp))
    )
}

@Composable
fun FeatureCard(
    badge: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MyColors.WhiteColor)
            .border(1.dp, MyColors.CardBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BadgeCircle(text = badge, size = 44)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            ReusableText(
                text = title,
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                color = MyColors.BlackColor
            )
            ReusableText(
                text = subtitle,
                fontSize = 13,
                color = MyColors.TextSecondaryColor
            )
        }
        ReusableText(
            text = "›",
            fontSize = 24,
            color = MyColors.TextSecondaryColor
        )
    }
}

@Composable
fun InfoRow(
    badge: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BadgeCircle(text = badge, size = 38)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {
            ReusableText(
                text = title,
                fontSize = 15,
                fontWeight = FontWeight.SemiBold,
                color = MyColors.BlackColor
            )
            ReusableText(
                text = subtitle,
                fontSize = 13,
                color = MyColors.TextSecondaryColor
            )
        }
    }
}

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MyColors.AccentColor,
            contentColor = MyColors.WhiteColor
        )
    ) {
        ReusableText(
            text = text,
            fontSize = 16,
            fontWeight = FontWeight.SemiBold,
            color = MyColors.WhiteColor
        )
    }
}

@Composable
private fun BadgeCircle(
    text: String,
    size: Int
) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MyColors.AccentSoftColor),
        contentAlignment = Alignment.Center
    ) {
        ReusableText(
            text = text,
            fontSize = if (size >= 44) 18 else 15,
            fontWeight = FontWeight.Bold,
            color = MyColors.AccentColor
        )
    }
}
