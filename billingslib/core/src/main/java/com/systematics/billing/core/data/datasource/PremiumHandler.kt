package com.systematics.billing.core.data.datasource

import android.app.Activity
import com.systematics.billing.core.domain.model.PremiumOffer
import com.systematics.billing.core.domain.model.PremiumState
import kotlinx.coroutines.flow.MutableStateFlow

interface PremiumHandler {

    val premiumStateChannel: MutableStateFlow<PremiumState>

    suspend fun queryPurchases(entitlementIds: List<String> = listOf())

    suspend fun buyPremiumOffer(
        activity: Activity,
        offerId: String,
        purchasedToken: String = "",
        onDone: (Boolean) -> Unit
    )

    suspend fun queryPremiumProducts(productIds: List<String>): List<PremiumOffer>
    suspend fun queryPremiumSubscriptions(subscriptionIds: List<String>): List<PremiumOffer>
}