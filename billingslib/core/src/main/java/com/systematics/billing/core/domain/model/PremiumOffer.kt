package com.systematics.billing.core.domain.model

import com.systematics.billing.core.domain.model.offer.period.PremiumOfferPhase
import com.systematics.billing.core.domain.model.offer.period.OfferTimePeriod
import com.systematics.billing.core.domain.model.offer.price.OfferPrice

sealed class PremiumOffer(
    open val id: String,
    open val name: String
) {
    data class InAppProduct(
        override val id: String,
        override val name: String,
        val price: OfferPrice,
        val period: OfferTimePeriod
    ) : PremiumOffer(id, name)

    data class Subscription(
        override val id: String,
        override val name: String,
        val paidPhases: List<PremiumOfferPhase>,
        val trialPhase: PremiumOfferPhase? = null
    ) : PremiumOffer(id, name)
}