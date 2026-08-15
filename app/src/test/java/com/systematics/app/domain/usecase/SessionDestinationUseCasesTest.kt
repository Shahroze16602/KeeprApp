package com.systematics.app.domain.usecase

import com.systematics.app.domain.model.LaunchDestination
import com.systematics.app.domain.model.PremiumOfferType
import com.systematics.app.domain.model.ThemeMode
import com.systematics.app.domain.repository.PreferencesRepository
import com.systematics.app.domain.repository.RemoteConfigRepository
import com.systematics.app.domain.repository.SessionGate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionDestinationUseCasesTest {
    @Test
    fun `session count rules preserve first-session semantics`() {
        val preferences = FakePreferences(firstSession = true)
        val config = FakeRemoteConfig(SessionGate.LANGUAGE to 1, SessionGate.ONBOARDING to 3)
        val isAllowed = IsSessionGateAllowedUseCase(preferences, config)

        assertTrue(isAllowed(SessionGate.LANGUAGE))
        assertFalse(isAllowed(SessionGate.ONBOARDING))

        preferences.firstSession = false

        assertFalse(isAllowed(SessionGate.LANGUAGE))
        assertTrue(isAllowed(SessionGate.ONBOARDING))
    }

    @Test
    fun `splash destination keeps premium language onboarding home precedence`() {
        val preferences = FakePreferences()
        val config = FakeRemoteConfig(
            SessionGate.PREMIUM_1 to 2,
            SessionGate.LANGUAGE to 2,
            SessionGate.ONBOARDING to 2
        )
        val resolve = ResolveSplashDestinationUseCase(IsSessionGateAllowedUseCase(preferences, config))

        assertEquals(LaunchDestination.PREMIUM, resolve())

        preferences.premium1PurchasedState = true
        assertEquals(LaunchDestination.LANGUAGE, resolve())

        config.values[SessionGate.LANGUAGE] = 0
        assertEquals(LaunchDestination.ONBOARDING, resolve())

        config.values[SessionGate.ONBOARDING] = 0
        assertEquals(LaunchDestination.HOME, resolve())
    }
}

private class FakePreferences(
    var firstSession: Boolean = true,
    var premium1PurchasedState: Boolean = false
) : PreferencesRepository {
    override fun selectedLanguageCode() = "en"
    override fun saveSelectedLanguage(code: String, name: String) = Unit
    override fun isFirstSession() = firstSession
    override fun markFirstSessionCompleted() { firstSession = false }
    override fun themeMode() = ThemeMode.SYSTEM
    override fun saveThemeMode(mode: ThemeMode) = Unit
    override fun isPremium1Purchased() = premium1PurchasedState
    override fun setPremium1Purchased(value: Boolean) { premium1PurchasedState = value }
    override fun setPremium2Purchased(value: Boolean) = Unit
    override fun setAdRemovalPurchased(value: Boolean) = Unit
    override fun markPremiumStatusResolved() = Unit
    override val isAppPurchased: StateFlow<Boolean> = MutableStateFlow(false)
    override val isAdsEnabled: StateFlow<Boolean> = MutableStateFlow(true)
    override val premiumStatusResolved: StateFlow<Boolean> = MutableStateFlow(false)
}

private class FakeRemoteConfig(vararg entries: Pair<SessionGate, Int>) : RemoteConfigRepository {
    val values = entries.toMap().toMutableMap()
    override suspend fun initialize() = Unit
    override fun sessionCount(type: SessionGate) = values[type] ?: 0
    override fun premiumOfferId(type: PremiumOfferType) = ""
}
