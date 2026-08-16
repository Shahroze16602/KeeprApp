package com.systematics.photocleaner.swipedelete.di

import com.systematics.photocleaner.swipedelete.presentation.screens.language.LanguageViewModel
import com.systematics.photocleaner.swipedelete.presentation.screens.premium.PremiumViewModel
import com.systematics.photocleaner.swipedelete.presentation.screens.privacy_policy.PrivacyPolicyViewModel
import com.systematics.photocleaner.swipedelete.presentation.screens.splash.SplashViewModel
import com.systematics.photocleaner.swipedelete.presentation.screens.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::LanguageViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::PremiumViewModel)
    viewModelOf(::PrivacyPolicyViewModel)
    viewModelOf(::SettingsViewModel)
}
