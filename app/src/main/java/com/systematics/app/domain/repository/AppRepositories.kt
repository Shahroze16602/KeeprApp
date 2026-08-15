package com.systematics.app.domain.repository

import com.systematics.app.domain.model.PremiumOfferType
import com.systematics.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.StateFlow

interface PreferencesRepository {
    fun selectedLanguageCode(): String
    fun saveSelectedLanguage(code: String, name: String)
    fun isFirstSession(): Boolean
    fun markFirstSessionCompleted()
    fun themeMode(): ThemeMode
    fun saveThemeMode(mode: ThemeMode)
    fun isPremium1Purchased(): Boolean
    fun setPremium1Purchased(value: Boolean)
    fun setPremium2Purchased(value: Boolean)
    fun setAdRemovalPurchased(value: Boolean)
    fun markPremiumStatusResolved()
    val isAppPurchased: StateFlow<Boolean>
    val isAdsEnabled: StateFlow<Boolean>
    val premiumStatusResolved: StateFlow<Boolean>
}

interface RemoteConfigRepository {
    val isInitialized: StateFlow<Boolean>
    suspend fun initialize()
    fun adsEnabled(): Boolean
    fun billingEnabled(): Boolean
    fun sessionCount(type: SessionGate): Int
    fun premiumOfferId(type: PremiumOfferType): String
}

enum class SessionGate { LANGUAGE, ONBOARDING, PREMIUM_1, SHIFT_SPLASH_AD_TO_PREMIUM }
