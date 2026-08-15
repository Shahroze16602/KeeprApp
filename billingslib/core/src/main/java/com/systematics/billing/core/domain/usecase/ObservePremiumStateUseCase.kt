package com.systematics.billing.core.domain.usecase

import com.systematics.billing.core.di.BillingDIContainer
import com.systematics.billing.core.domain.model.PremiumState
import kotlinx.coroutines.flow.Flow

class ObservePremiumStateUseCase {

    operator fun invoke(): Flow<PremiumState> {
        return BillingDIContainer.premiumRepository.premiumStateChannel
    }
}