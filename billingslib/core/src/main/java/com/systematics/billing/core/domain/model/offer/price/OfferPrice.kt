package com.systematics.billing.core.domain.model.offer.price

data class OfferPrice(
    val currency: String,
    val priceMicros: Long,
    val formattedPrice: String
)