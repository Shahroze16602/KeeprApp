package com.systematics.billing.core.domain.model

sealed interface PremiumState {

    data object Checking : PremiumState
    data object NonPremium : PremiumState
    data object Unknown : PremiumState

    /** A real, observed failure after retries were exhausted (or a terminal error). Never emitted for transient/offline blips. */
    data class Error(val message: String) : PremiumState
    data class Premium(val subscribedOffer: SubscribedOffer) : PremiumState
}