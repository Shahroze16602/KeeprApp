package com.systematics.billing.core.domain.repositories

import android.app.Activity
import com.systematics.billing.core.domain.model.PremiumOffer
import com.systematics.billing.core.domain.model.PremiumState
import com.systematics.billing.core.utils.ResultResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface PremiumRepository {

    val premiumStateChannel: MutableStateFlow<PremiumState>

    suspend fun queryPurchases(entitlementIds: List<String> = listOf())

    suspend fun buyPremiumOffer(
        activity: Activity,
        premiumOffer: PremiumOffer,
        purchasedToken: String,
        onDone: (Boolean) -> Unit
    )

    suspend fun getPremiumOffers(
        productIds: List<String> = listOf(),
        subscriptionIds: List<String> = listOf()
    ): Flow<ResultResource<List<PremiumOffer>>>
}