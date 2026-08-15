package com.systematics.billing.core.data.repository


import android.app.Activity
import android.util.Log
import com.systematics.billing.core.data.datasource.PremiumHandler
import com.systematics.billing.core.domain.model.PremiumOffer
import com.systematics.billing.core.domain.model.PremiumState
import com.systematics.billing.core.domain.repositories.PremiumRepository
import com.systematics.billing.core.utils.ResultResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class PremiumRepositoryImpl(
    private val premiumHandler: PremiumHandler
) : PremiumRepository {

    override val premiumStateChannel: MutableStateFlow<PremiumState> =
        premiumHandler.premiumStateChannel

    override suspend fun queryPurchases(entitlementIds: List<String>) {
        premiumHandler.queryPurchases(entitlementIds)
    }

    override suspend fun buyPremiumOffer(
        activity: Activity,
        premiumOffer: PremiumOffer,
        purchasedToken: String,
        onDone: (Boolean) -> Unit
    ) {
        premiumHandler.buyPremiumOffer(activity, premiumOffer.id, purchasedToken, onDone)
    }

    override suspend fun getPremiumOffers(
        productIds: List<String>,
        subscriptionIds: List<String>
    ): Flow<ResultResource<List<PremiumOffer>>> =
        flow {
            emit(ResultResource.Loading())

            // Run each branch independently so a failure in one doesn't hide a success in the other.
            // The handler/client layer already retries transient failures, so a failure reaching here
            // is a real, surfaced error — not a transient blip.
            val products = runCatching { premiumHandler.queryPremiumProducts(productIds) }.normalizeEmptyIds()
            val subs = runCatching { premiumHandler.queryPremiumSubscriptions(subscriptionIds) }.normalizeEmptyIds()

            if (products.isFailure && subs.isFailure) {
                // Both genuinely failed -> surface the error instead of an ambiguous empty Success.
                val message = products.exceptionOrNull()?.message
                    ?: subs.exceptionOrNull()?.message
                    ?: "Failed to load offers"
                Log.e(TAG, "getPremiumOffers: both queries failed", products.exceptionOrNull())
                emit(ResultResource.Error(message))
            } else {
                // At least one succeeded (possibly empty) -> partial/full success.
                val combined = (products.getOrDefault(listOf()) + subs.getOrDefault(listOf()))
                    .distinctBy { it.id }
                Log.d(TAG, "getPremiumOffers: $combined")
                emit(ResultResource.Success(combined))
            }
        }

    /** Empty/blank ids are a caller choice, not a failure — treat as an empty success, not an error. */
    private fun Result<List<PremiumOffer>>.normalizeEmptyIds(): Result<List<PremiumOffer>> =
        if (exceptionOrNull() is IllegalArgumentException) Result.success(listOf()) else this

    companion object {
        private const val TAG = "PremiumRepositoryImplTAG"
    }
}
