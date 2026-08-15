package com.systematics.billing.core.domain.usecase

import com.systematics.billing.core.di.BillingDIContainer
import com.systematics.billing.core.domain.model.PremiumOffer
import com.systematics.billing.core.utils.ResultResource
import kotlinx.coroutines.flow.Flow

class QueryPremiumOffersUseCase {

    suspend operator fun invoke(
        productIds: List<String>,
        subscriptionIds: List<String>
    ): Flow<ResultResource<List<PremiumOffer>>> {
        return BillingDIContainer.premiumRepository.getPremiumOffers(
            productIds = productIds,
            subscriptionIds = subscriptionIds
        )
    }
}