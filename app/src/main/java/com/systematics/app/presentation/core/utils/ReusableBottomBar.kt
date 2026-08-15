package com.systematics.app.presentation.core.utils

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.systematics.app.R

enum class MainTab(@param:StringRes val label: Int, @param:DrawableRes val icon: Int) {
    Home(label = R.string.home, icon = R.drawable.ic_home)
}

@Composable
fun ReusableBottomBar(
    selected: MainTab?,
    onSelect: (MainTab) -> Unit,
) {
    NavigationBar {
        MainTab.entries.forEach { tab ->
            val labelText = stringResource(tab.label)
            NavigationBarItem(
                selected = selected == tab,
                onClick = { onSelect(tab) },
                icon = { ReusableImage(painter = tab.icon, modifier = Modifier.size(24.dp)) },
                label = { Text(labelText) },
            )
        }
    }
}