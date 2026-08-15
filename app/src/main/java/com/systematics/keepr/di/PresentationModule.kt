package com.systematics.keepr.di

import com.systematics.keepr.presentation.screens.language.LanguageViewModel
import com.systematics.keepr.presentation.screens.premium.PremiumViewModel
import com.systematics.keepr.presentation.screens.privacy_policy.PrivacyPolicyViewModel
import com.systematics.keepr.presentation.screens.splash.SplashViewModel
import com.systematics.keepr.presentation.screens.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val presentationModule = module {
    viewModelOf(::LanguageViewModel)
    viewModelOf(::SplashViewModel)
    viewModelOf(::PremiumViewModel)
    viewModelOf(::PrivacyPolicyViewModel)
    viewModelOf(::SettingsViewModel)
}
