package com.systematics.photocleaner.swipedelete.presentation.screens.settings

import androidx.lifecycle.ViewModel
import com.systematics.photocleaner.swipedelete.domain.model.ThemeMode
import com.systematics.photocleaner.swipedelete.domain.usecase.GetThemeModeUseCase
import com.systematics.photocleaner.swipedelete.domain.usecase.SaveThemeModeUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    getThemeMode: GetThemeModeUseCase,
    private val saveThemeMode: SaveThemeModeUseCase
) : ViewModel() {
    private val _themeMode = MutableStateFlow(getThemeMode())
    val themeMode = _themeMode.asStateFlow()

    fun selectTheme(mode: ThemeMode) {
        if (_themeMode.value != mode) {
            _themeMode.value = mode
            saveThemeMode(mode)
        }
    }
}
