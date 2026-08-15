package com.systematics.keepr.utils.core

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLocaleManager {
    fun applyLocale(languageCode: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageCode.ifBlank { DEFAULT_LANGUAGE_CODE })
        )
    }

    private const val DEFAULT_LANGUAGE_CODE = "en"
}
