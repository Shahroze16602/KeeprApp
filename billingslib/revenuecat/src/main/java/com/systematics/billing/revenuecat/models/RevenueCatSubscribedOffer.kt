package com.systematics.billing.revenuecat.models

import com.systematics.billing.core.domain.model.SubscribedOffer
import com.revenuecat.purchases.EntitlementInfo

data class RevenueCatSubscribedOffer(
    val entitlements: List<EntitlementInfo>
) : SubscribedOffer