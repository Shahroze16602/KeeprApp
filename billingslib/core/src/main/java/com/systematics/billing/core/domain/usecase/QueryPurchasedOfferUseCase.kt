package com.systematics.billing.core.domain.usecase

import com.systematics.billing.core.di.BillingDIContainer

class QueryPurchasedOfferUseCase {

    suspend operator fun invoke(entitlementIds: List<String> = listOf()) {
        BillingDIContainer.premiumRepository.queryPurchases(entitlementIds)
    }
}