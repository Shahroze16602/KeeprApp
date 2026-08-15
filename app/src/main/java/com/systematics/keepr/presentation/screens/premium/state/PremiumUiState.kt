package com.systematics.keepr.presentation.screens.premium.state

import com.systematics.billing.core.domain.model.PremiumOffer

data class PremiumUiState(
    val isCloseVisible: Boolean = true,
    val bestValueId: String = "",
    val premiumOffers: List<PremiumOffer> = listOf(),
    val selectedOffer: PremiumOffer? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
