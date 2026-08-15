package com.systematics.app.presentation.screens.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.systematics.app.R
import com.systematics.app.utils.core.MyColors
import com.systematics.app.domain.model.ThemeMode
import com.systematics.app.presentation.core.utils.ReusableText
import com.systematics.app.presentation.core.utils.ReusableTopBar
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel()
) {
    val selected by viewModel.themeMode.collectAsStateWithLifecycle()

    val onSelect: (ThemeMode) -> Unit = { mode ->
        if (mode != selected) {
            viewModel.selectTheme(mode)
            AppCompatDelegate.setDefaultNightMode(mode.nightMode)
        }
    }

    Scaffold(
        containerColor = MyColors.BackgroundColor,
        topBar = {
            ReusableTopBar(
                titleRes = R.string.settings,
                onBackClick = onBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp)
        ) {
            ReusableText(
                text = "Theme",
                fontSize = 16,
                fontWeight = FontWeight.SemiBold,
                color = MyColors.BlackColor
            )

            ThemeOptionRow("System default", selected == ThemeMode.SYSTEM) {
                onSelect(ThemeMode.SYSTEM)
            }
            ThemeOptionRow("Light", selected == ThemeMode.LIGHT) {
                onSelect(ThemeMode.LIGHT)
            }
            ThemeOptionRow("Dark", selected == ThemeMode.DARK) {
                onSelect(ThemeMode.DARK)
            }
        }
    }
}

private val ThemeMode.nightMode: Int
    get() = when (this) {
        ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
        ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
    }

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MyColors.WhiteColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ReusableText(
            modifier = Modifier.weight(1f),
            text = label,
            fontSize = 15,
            color = MyColors.BlackColor
        )
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = MyColors.AccentColor)
        )
    }
}
