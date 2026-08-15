package com.systematics.billing.core.domain.model.offer.period

sealed interface OfferTimePeriod {

    data object Lifetime : OfferTimePeriod
    data class Timed(val period: OfferPeriod)
}