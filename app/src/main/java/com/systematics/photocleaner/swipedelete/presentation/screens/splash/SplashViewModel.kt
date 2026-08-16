package com.systematics.photocleaner.swipedelete.presentation.screens.splash

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systematics.monetization.core.interfaces.InitializationStatus
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.utils.monetizationInject
import com.systematics.photocleaner.swipedelete.domain.usecase.InitializeRemoteConfigUseCase
import com.systematics.photocleaner.swipedelete.domain.usecase.IsAdsEnabledUseCase
import com.systematics.photocleaner.swipedelete.domain.usecase.ObservePremiumStatusResolvedUseCase
import com.systematics.photocleaner.swipedelete.utils.monetization.MonetizationHandler
import com.systematics.photocleaner.swipedelete.presentation.screens.splash.events.SplashOneTimeUiEvents
import com.systematics.photocleaner.swipedelete.presentation.screens.splash.state.SplashUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SplashViewModel(
    private val initializeRemoteConfig: InitializeRemoteConfigUseCase,
    private val observePremiumStatusResolved: ObservePremiumStatusResolvedUseCase,
    private val isAdsEnabled: IsAdsEnabledUseCase
) : ViewModel(), KoinComponent {
    private val monetizationHandler: MonetizationHandler by inject()
    private val monetizationInstall: MonetizationInstall by monetizationInject()

    private val _state = MutableStateFlow(SplashUiState())
    val state = _state.asStateFlow()

    private var splashInitializationCalled = false

    private val _oneTimeUiEvents = Channel<SplashOneTimeUiEvents>()
    val oneTimeUiEvents: Flow<SplashOneTimeUiEvents> = _oneTimeUiEvents.receiveAsFlow()

    private val remoteFetched = MutableStateFlow(false)
    private val monetizationInitialized = MutableStateFlow(false)
    private var networkSetupNotified = false

    private val splashSetupDone = combine(
        remoteFetched,
        monetizationInitialized,
        observePremiumStatusResolved()
    ) { remoteReady, monetizationReady, premiumResolved ->
        if (remoteReady && monetizationReady && premiumResolved && !networkSetupNotified) {
            networkSetupNotified = true
            if (isAdsEnabled()) {
                monetizationInstall.updateFromRemote()
            }
            onNetworkSetupDone()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    fun initializeSplash(activity: Activity) {
        if (!splashInitializationCalled) {
            initializeRemoteConfig(activity)
            splashInitializationCalled = true
        } else {
            Log.d(
                TAG, "initializeSplash: splash initialization already called"
            )
        }
    }

    fun onSplashAdDone(adShown: Boolean) {
        Log.d(
            TAG, "onSplashAdDone: adShown $adShown"
        )
        viewModelScope.launch { _oneTimeUiEvents.send(SplashOneTimeUiEvents.SplashDone(adShown)) }
    }

    private fun initializeRemoteConfig(activity: Activity) {
        viewModelScope.launch {
            runCatching { initializeRemoteConfig.invoke() }
                .onFailure { Log.e(TAG, "initializeRemoteConfig: failed", it) }
            if (isAdsEnabled()) {
                monetizationHandler.setupMonetization(activity.application)
                initializeMonetization(activity)
            } else {
                monetizationInitialized.value = true
            }
            remoteFetched.value = true
        }
    }

    private fun initializeMonetization(activity: Activity) {
        Log.d(TAG, "initializeMonetization: monetization initializing")
        viewModelScope.launch {
            monetizationHandler.observeInitialization().collectLatest { status ->
                when (status) {
                    InitializationStatus.Deferred, InitializationStatus.Failed, InitializationStatus.Success -> {
                        Log.d(TAG, "initializeMonetization: Done")
                        monetizationInitialized.value = true
                    }

                    else -> Unit
                }
            }
        }
        monetizationHandler.initMonetizationOnActivity(activity)
    }

    private fun onNetworkSetupDone() {
        Log.d(
            TAG, "onNetworkSetupDone: "
        )
        viewModelScope.launch {
            _oneTimeUiEvents.send(SplashOneTimeUiEvents.NetworkSetupDone)
        }
    }


    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "onCleared: ")
    }

    companion object {

        private const val TAG = "SplashViewModelTAG"
    }
}
