package com.systematics.keepr.presentation.screens.language

import androidx.lifecycle.ViewModel
import com.systematics.keepr.utils.core.AppLanguageModel
import com.systematics.keepr.domain.usecase.GetStoredLanguageUseCase
import com.systematics.keepr.domain.usecase.SaveSelectedLanguageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class LanguageUiState(
    val searchText: String = "",
    val selectedLanguage: String = "en",
    val languages: List<AppLanguageModel> = AppLanguageModel.entries
) {
    fun filteredLanguages(): List<AppLanguageModel> {
        val query = searchText.trim()
        return languages
            .filter { language ->
                query.isBlank() ||
                    language.languageName.contains(query, ignoreCase = true) ||
                    language.languageDes.contains(query, ignoreCase = true) ||
                    language.languageAbbr.contains(query, ignoreCase = true)
            }
            .sortedBy { it.languageName.lowercase(Locale.getDefault()) }
    }
}

class LanguageViewModel(
    private val getStoredLanguage: GetStoredLanguageUseCase,
    private val saveSelectedLanguage: SaveSelectedLanguageUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(
        LanguageUiState(
            selectedLanguage = getStoredLanguage().ifBlank { "en" }
        )
    )
    val state = _state.asStateFlow()

    fun onSearchChange(value: String) {
        _state.update { current ->
            current.copy(searchText = value)
        }
    }

    fun onLanguageSelected(language: AppLanguageModel) {
        _state.update { current ->
            current.copy(selectedLanguage = language.languageAbbr)
        }
    }

    fun persistSelection(): String {
        val selectedCode = state.value.selectedLanguage
        val selectedLanguage = AppLanguageModel.entries.firstOrNull {
            it.languageAbbr == selectedCode
        }

        val code = selectedLanguage?.languageAbbr ?: "en"
        saveSelectedLanguage(code, selectedLanguage?.languageName ?: "English")
        return code
    }
}
