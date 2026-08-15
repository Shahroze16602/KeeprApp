package com.systematics.billing.core.domain.model.offer.period

import com.systematics.billing.core.domain.model.offer.price.OfferPrice

data class PremiumOfferPhase(
    val price: OfferPrice,
    val period: OfferPeriod
)