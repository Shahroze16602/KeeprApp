package com.systematics.app.di

import com.systematics.app.presentation.screens.language.LanguageViewModel
import com.systematics.app.presentation.screens.premium.PremiumViewModel
import com.systematics.app.presentation.screens.privacy_policy.PrivacyPolicyViewModel
import com.systematics.app.presentation.screens.splash.SplashViewModel
import com.systematics.app.presentation.screens.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::LanguageViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::PremiumViewModel)
    viewModelOf(::PrivacyPolicyViewModel)
    viewModelOf(::SettingsViewModel)
}
