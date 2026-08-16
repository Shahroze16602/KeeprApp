package com.systematics.photocleaner.swipedelete.presentation.screens.privacy_policy

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class StatePrivacyPolicyScreen(
    val privacyPolicyLink: String? = null,
    val isLoading: Boolean = true
)

class PrivacyPolicyViewModel : ViewModel() {

    private val _state = MutableStateFlow(StatePrivacyPolicyScreen())
    val state get() = _state.asStateFlow()

    init {
        privacyPolicyLink()
    }

    private fun privacyPolicyLink(link: String = PRIVACY_POLICY_URL) {
        _state.update {
            it.copy(
                privacyPolicyLink = link
            )
        }
    }

    fun isLoading(show: Boolean = false) {
        _state.update {
            it.copy(
                isLoading = show
            )
        }
    }

    private companion object {
        const val PRIVACY_POLICY_URL = "https://sites.google.com/view/keepr-privacy-policy/home"
    }
}
