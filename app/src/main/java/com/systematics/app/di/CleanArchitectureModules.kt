package com.systematics.app.di

import com.systematics.app.data.datasource.FirebaseConfigDataSource
import com.systematics.app.data.datasource.SharedPreferencesDataSource
import com.systematics.app.data.repository.PreferencesRepositoryImpl
import com.systematics.app.data.repository.RemoteConfigRepositoryImpl
import com.systematics.app.domain.repository.PreferencesRepository
import com.systematics.app.domain.repository.RemoteConfigRepository
import com.systematics.app.domain.usecase.*
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val dataModule = module {
    singleOf(::SharedPreferencesDataSource)
    singleOf(::FirebaseConfigDataSource)
    singleOf(::PreferencesRepositoryImpl) bind PreferencesRepository::class
    singleOf(::RemoteConfigRepositoryImpl) bind RemoteConfigRepository::class
}

val domainModule = module {
    factoryOf(::InitializeRemoteConfigUseCase)
    factoryOf(::ObserveRemoteConfigInitializedUseCase)
    factoryOf(::IsAdsEnabledUseCase)
    factoryOf(::IsBillingEnabledUseCase)
    factoryOf(::GetPremiumOfferIdUseCase)
    factoryOf(::GetThemeModeUseCase)
    factoryOf(::SaveThemeModeUseCase)
    factoryOf(::MarkFirstSessionCompletedUseCase)
    factoryOf(::SaveSelectedLanguageUseCase)
    factoryOf(::GetStoredLanguageUseCase)
    factoryOf(::ObserveAppPurchasedUseCase)
    factoryOf(::ObservePremiumStatusResolvedUseCase)
    factoryOf(::IsPremium1PurchasedUseCase)
    factoryOf(::SetPremium1PurchasedUseCase)
    factoryOf(::SetPremium2PurchasedUseCase)
    factoryOf(::SetAdRemovalPurchasedUseCase)
    factoryOf(::MarkPremiumStatusResolvedUseCase)
    factoryOf(::IsSessionGateAllowedUseCase)
    factoryOf(::IsShiftSplashAdToPremiumUseCase)
    factoryOf(::ResolveSplashDestinationUseCase)
    factoryOf(::ResolvePremiumNextDestinationUseCase)
    factoryOf(::ResolveLanguageNextDestinationUseCase)
}
