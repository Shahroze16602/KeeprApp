package com.systematics.app.data.repository

import com.systematics.app.data.datasource.FirebaseConfigDataSource
import com.systematics.app.data.datasource.SharedPreferencesDataSource
import com.systematics.app.domain.model.PremiumOfferType
import com.systematics.app.domain.model.ThemeMode
import com.systematics.app.domain.repository.PreferencesRepository
import com.systematics.app.domain.repository.RemoteConfigRepository
import com.systematics.app.domain.repository.SessionGate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepositoryImpl(private val dataSource: SharedPreferencesDataSource) : PreferencesRepository {
    private val appPurchased = MutableStateFlow(currentAppPurchased())
    private val adsEnabled = MutableStateFlow(!dataSource.boolean(KEY_AD_REMOVAL_PURCHASED, false))
    private val premiumResolved = MutableStateFlow(false)

    override fun selectedLanguageCode() = dataSource.string(KEY_LANGUAGE_CODE, DEFAULT_LANGUAGE_CODE)
    override fun saveSelectedLanguage(code: String, name: String) {
        dataSource.putString(KEY_LANGUAGE_CODE, code)
        dataSource.putString(KEY_LANGUAGE_NAME, name)
    }
    override fun isFirstSession() = dataSource.boolean(KEY_FIRST_SESSION, true)
    override fun markFirstSessionCompleted() = dataSource.putBoolean(KEY_FIRST_SESSION, false)
    override fun themeMode() = runCatching {
        ThemeMode.valueOf(dataSource.string(KEY_THEME_MODE, ThemeMode.SYSTEM.name))
    }.getOrDefault(ThemeMode.SYSTEM)
    override fun saveThemeMode(mode: ThemeMode) = dataSource.putString(KEY_THEME_MODE, mode.name)
    override fun isPremium1Purchased() = dataSource.boolean(KEY_PREMIUM_1_PURCHASED, false)
    override fun setPremium1Purchased(value: Boolean) { dataSource.putBoolean(KEY_PREMIUM_1_PURCHASED, value); appPurchased.value = currentAppPurchased() }
    override fun setPremium2Purchased(value: Boolean) { dataSource.putBoolean(KEY_PREMIUM_2_PURCHASED, value); appPurchased.value = currentAppPurchased() }
    override fun setAdRemovalPurchased(value: Boolean) { dataSource.putBoolean(KEY_AD_REMOVAL_PURCHASED, value); adsEnabled.value = !value }
    override fun markPremiumStatusResolved() { premiumResolved.value = true }
    override val isAppPurchased: StateFlow<Boolean> = appPurchased.asStateFlow()
    override val isAdsEnabled: StateFlow<Boolean> = adsEnabled.asStateFlow()
    override val premiumStatusResolved: StateFlow<Boolean> = premiumResolved.asStateFlow()

    private fun currentAppPurchased() = isPremium1Purchased() && dataSource.boolean(KEY_PREMIUM_2_PURCHASED, false)
    private companion object {
        const val KEY_LANGUAGE_CODE = "selectedLanguageKey"
        const val KEY_LANGUAGE_NAME = "selectedLanguageName"
        const val KEY_FIRST_SESSION = "isFirstSession"
        const val KEY_THEME_MODE = "themeMode"
        const val KEY_PREMIUM_1_PURCHASED = "isPremium1Purchased"
        const val KEY_PREMIUM_2_PURCHASED = "isPremium2Purchased"
        const val KEY_AD_REMOVAL_PURCHASED = "isAdAppPurchased"
        const val DEFAULT_LANGUAGE_CODE = "en"
    }
}

class RemoteConfigRepositoryImpl(private val dataSource: FirebaseConfigDataSource) : RemoteConfigRepository {
    private val initialized = MutableStateFlow(false)

    override val isInitialized: StateFlow<Boolean> = initialized.asStateFlow()

    override suspend fun initialize() {
        try {
            dataSource.fetchAndActivate()
        } finally {
            initialized.value = true
        }
    }
    override fun adsEnabled() = dataSource.boolean(KEY_ADS_ENABLED, default = true)
    override fun billingEnabled() = dataSource.boolean(KEY_BILLING_ENABLED, default = true)
    override fun sessionCount(type: SessionGate) = dataSource.long(when (type) {
        SessionGate.LANGUAGE -> KEY_LANGUAGE_SESSION_COUNT
        SessionGate.ONBOARDING -> KEY_ONBOARDING_SESSION_COUNT
        SessionGate.PREMIUM_1 -> KEY_PREMIUM_1_SESSION_COUNT
        SessionGate.SHIFT_SPLASH_AD_TO_PREMIUM -> KEY_SHIFT_SPLASH_AD_TO_PREMIUM
    })
    override fun premiumOfferId(type: PremiumOfferType) = dataSource.string(when (type) {
        PremiumOfferType.PREMIUM_1 -> KEY_PREMIUM_1_OFFER_ID
        PremiumOfferType.PREMIUM_2 -> KEY_PREMIUM_2_OFFER_ID
        PremiumOfferType.REMOVE_ADS -> KEY_REMOVE_ADS_OFFER_ID
    })
    private companion object {
        const val KEY_ADS_ENABLED = "ads_enabled"
        const val KEY_BILLING_ENABLED = "billing_enabled"
        const val KEY_LANGUAGE_SESSION_COUNT = "language_session_count"
        const val KEY_ONBOARDING_SESSION_COUNT = "onboarding_session_count"
        const val KEY_PREMIUM_1_SESSION_COUNT = "premium_1_session_count"
        const val KEY_SHIFT_SPLASH_AD_TO_PREMIUM = "shift_splash_ad_to_premium"
        const val KEY_PREMIUM_1_OFFER_ID = "premium_1_offer_id"
        const val KEY_PREMIUM_2_OFFER_ID = "premium_2_offer_id"
        const val KEY_REMOVE_ADS_OFFER_ID = "premium_ads_remove_offer_id"
    }
}
