package com.systematics.app.utils.core

import com.systematics.app.R

enum class AppLanguageModel(
    var languageFlag: Int,
    var languageName: String,
    var languageAbbr: String,
    var languageDes: String,
) {
    Arabic(
        R.drawable.flag_arabic, "Arabic", "ar", "العربية"
    ),
    English(
        R.drawable.flag_english, "English", "en", "English"
    ),
    French(
        R.drawable.flag_french, "French", "fr", "Français"
    ),
    German(
        R.drawable.flag_germany, "German", "de", "Deutsch"
    ),
    Hebrew(
        R.drawable.flag_israel, "Hebrew", "he", "עברית"
    ),
    Hindi(
        R.drawable.flag_hindi, "Hindi", "hi", "हिन्दी"
    ),
    Indonesian(
        R.drawable.flag_indonesia, "Indonesian", "in", "Bahasa Indonesia"
    ),
    Italian(
        R.drawable.flag_italian, "Italian", "it", "Italiano"
    ),
    Japanese(
        R.drawable.flag_japan, "Japanese", "ja", "日本語"
    ),
    Korean(
        R.drawable.flag_south_korea, "Korean", "ko", "한국어"
    ),
    Malaysian(
        R.drawable.flag_malaysia, "Malaysian", "ms", "Bahasa Malaysia"
    ),
    Portuguese(
        R.drawable.flag_portugal, "Portuguese", "pt", "Português"
    ),
    Russian(
        R.drawable.flag_russia, "Russian", "ru", "Русский"
    ),
    Spanish(
        R.drawable.flag_spanish, "Spanish", "es", "Español"
    ),
    Thai(
        R.drawable.flag_thailand, "Thai", "th", "ภาษาไทย"
    ),
    Turkish(
        R.drawable.flag_turkey, "Turkish", "tr", "Türkçe"
    ),
    Vietnamese(
        R.drawable.flag_vietnam, "Vietnamese", "vi", "Tiếng Việt"
    )
}