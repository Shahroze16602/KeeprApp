package com.systematics.billing.play.models

import com.android.billingclient.api.Purchase
import com.systematics.billing.core.domain.model.SubscribedOffer

data class PlaySubscribedOffer(
    val purchases: List<Purchase>
) : SubscribedOffer