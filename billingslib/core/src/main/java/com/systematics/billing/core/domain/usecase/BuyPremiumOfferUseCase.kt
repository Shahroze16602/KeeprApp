package com.systematics.billing.core.domain.usecase

import android.app.Activity
import com.systematics.billing.core.di.BillingDIContainer
import com.systematics.billing.core.domain.model.PremiumOffer

class BuyPremiumOfferUseCase {

    suspend operator fun invoke(
        activity: Activity,
        premiumOffer: PremiumOffer,
        purchasedToken: String = "",
        onDone: (Boolean) -> Unit
    ) {
        BillingDIContainer.premiumRepository.buyPremiumOffer(
            activity,
            premiumOffer,
            purchasedToken,
            onDone
        )
    }
}