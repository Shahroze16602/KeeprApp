package com.systematics.app.presentation.screens.premium.event

import android.app.Activity
import com.systematics.billing.core.domain.model.PremiumOffer
import com.systematics.app.presentation.screens.premium.PremiumTarget

sealed interface PremiumEvents {
    data class PremiumBuyRequested(val activity: Activity, val onDone: (success: Boolean) -> Unit) : PremiumEvents
    data class OnPremiumOfferSelected(val premiumOffer: PremiumOffer?) : PremiumEvents
    data class OnPremiumPurchaseTarget(val activity: Activity, val premiumTarget: PremiumTarget, val onDone: (success: Boolean) -> Unit) :
        PremiumEvents
}