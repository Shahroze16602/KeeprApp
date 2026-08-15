package com.systematics.keepr.domain.usecase

import com.systematics.keepr.domain.model.LaunchDestination
import com.systematics.keepr.domain.model.PremiumOfferType
import com.systematics.keepr.domain.model.ThemeMode
import com.systematics.keepr.domain.repository.PreferencesRepository
import com.systematics.keepr.domain.repository.RemoteConfigRepository
import com.systematics.keepr.domain.repository.SessionGate

class InitializeRemoteConfigUseCase(private val repository: RemoteConfigRepository) {
    suspend operator fun invoke() = repository.initialize()
}

class ObserveRemoteConfigInitializedUseCase(private val repository: RemoteConfigRepository) {
    operator fun invoke() = repository.isInitialized
}

class IsAdsEnabledUseCase(private val repository: RemoteConfigRepository, private val preferences: PreferencesRepository) {
    operator fun invoke() = repository.adsEnabled() && preferences.isAdsEnabled.value
}

class IsBillingEnabledUseCase(private val repository: RemoteConfigRepository) {
    operator fun invoke() = repository.billingEnabled()
}

class GetPremiumOfferIdUseCase(private val repository: RemoteConfigRepository) {
    operator fun invoke(type: PremiumOfferType) = repository.premiumOfferId(type)
}

class GetThemeModeUseCase(private val repository: PreferencesRepository) {
    operator fun invoke() = repository.themeMode()
}

class SaveThemeModeUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(mode: ThemeMode) = repository.saveThemeMode(mode)
}

class MarkFirstSessionCompletedUseCase(private val repository: PreferencesRepository) {
    operator fun invoke() = repository.markFirstSessionCompleted()
}

class SaveSelectedLanguageUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(code: String, name: String) = repository.saveSelectedLanguage(code, name)
}

class GetStoredLanguageUseCase(private val repository: PreferencesRepository) {
    operator fun invoke() = repository.selectedLanguageCode()
}

class ObserveAppPurchasedUseCase(private val repository: PreferencesRepository) {
    operator fun invoke() = repository.isAppPurchased
}

class ObservePremiumStatusResolvedUseCase(private val repository: PreferencesRepository) {
    operator fun invoke() = repository.premiumStatusResolved
}

class IsPremium1PurchasedUseCase(private val repository: PreferencesRepository) {
    operator fun invoke() = repository.isPremium1Purchased()
}

class SetPremium1PurchasedUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(value: Boolean) = repository.setPremium1Purchased(value)
}

class SetPremium2PurchasedUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(value: Boolean) = repository.setPremium2Purchased(value)
}

class SetAdRemovalPurchasedUseCase(private val repository: PreferencesRepository) {
    operator fun invoke(value: Boolean) = repository.setAdRemovalPurchased(value)
}

class MarkPremiumStatusResolvedUseCase(private val repository: PreferencesRepository) {
    operator fun invoke() = repository.markPremiumStatusResolved()
}

class IsSessionGateAllowedUseCase(
    private val preferences: PreferencesRepository,
    private val remoteConfig: RemoteConfigRepository
) {
    operator fun invoke(gate: SessionGate): Boolean {
        if (gate == SessionGate.PREMIUM_1 && preferences.isPremium1Purchased()) return false
        return when (remoteConfig.sessionCount(gate)) {
            0 -> false
            1 -> preferences.isFirstSession()
            2 -> true
            3 -> !preferences.isFirstSession()
            else -> true
        }
    }
}

class IsShiftSplashAdToPremiumUseCase(
    private val isAllowed: IsSessionGateAllowedUseCase
) {
    operator fun invoke() = isAllowed(SessionGate.PREMIUM_1) && isAllowed(SessionGate.SHIFT_SPLASH_AD_TO_PREMIUM)
}

class ResolveSplashDestinationUseCase(private val isAllowed: IsSessionGateAllowedUseCase) {
    operator fun invoke() = when {
        isAllowed(SessionGate.PREMIUM_1) -> LaunchDestination.PREMIUM
        isAllowed(SessionGate.LANGUAGE) -> LaunchDestination.LANGUAGE
        isAllowed(SessionGate.ONBOARDING) -> LaunchDestination.ONBOARDING
        else -> LaunchDestination.HOME
    }
}

class ResolvePremiumNextDestinationUseCase(private val isAllowed: IsSessionGateAllowedUseCase) {
    operator fun invoke() = when {
        isAllowed(SessionGate.LANGUAGE) -> LaunchDestination.LANGUAGE
        isAllowed(SessionGate.ONBOARDING) -> LaunchDestination.ONBOARDING
        else -> LaunchDestination.HOME
    }
}

class ResolveLanguageNextDestinationUseCase(private val isAllowed: IsSessionGateAllowedUseCase) {
    operator fun invoke() = if (isAllowed(SessionGate.ONBOARDING)) LaunchDestination.ONBOARDING else LaunchDestination.HOME
}
