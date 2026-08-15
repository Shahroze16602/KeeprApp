package com.systematics.billing.revenuecat

import android.app.Activity
import android.content.Context
import android.util.Log
import com.systematics.billing.core.connectivity.AndroidConnectivityProvider
import com.systematics.billing.core.data.datasource.PremiumHandler
import com.systematics.billing.core.domain.connectivity.ConnectivityProvider
import com.systematics.billing.core.domain.model.PremiumOffer
import com.systematics.billing.core.domain.model.PremiumState
import com.systematics.billing.core.utils.retry
import com.systematics.billing.revenuecat.models.RevenueCatSubscribedOffer
import com.systematics.billing.revenuecat.utils.toInApp
import com.systematics.billing.revenuecat.utils.toSubscription
import com.revenuecat.purchases.Package
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RevenueCatBillingPremiumHandler(
    context: Context,
    private val connectivity: ConnectivityProvider = AndroidConnectivityProvider(context)
) : PremiumHandler {
    override val premiumStateChannel: MutableStateFlow<PremiumState> =
        MutableStateFlow(PremiumState.Unknown)
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val loadedProducts: HashMap<String, Package> = hashMapOf()
    private val loadedSubscriptions: HashMap<String, Package> = hashMapOf()
    private val revenueCatBillingClient = RevenueCatBillingClient(context)

    companion object {
        private const val TAG = "RevenueCatBillingPremiumHandlerTAG"
        // Programming/precondition errors must never loop; everything else is retried within the cap.
        private val isRetryable: (Throwable) -> Boolean =
            { it !is IllegalArgumentException && it !is IllegalStateException }
    }

    override suspend fun queryPurchases(entitlementIds: List<String>) {
        // Wait for internet so the entitlement restore doesn't fail offline.
        connectivity.awaitConnected()
        revenueCatBillingClient.queryPurchases(
            entitlementIds = entitlementIds,
            onPurchasesFound = {
                Log.d(TAG, "queryPurchases: onPurchasesFound $it")
                onPremiumStateUpdated(PremiumState.Premium(RevenueCatSubscribedOffer(it)))
            },
            onNoPurchasesFound = {
                Log.d(TAG, "queryPurchases: onNoPurchasesFound")
                onPremiumStateUpdated(PremiumState.NonPremium)
            },
            onFailed = {
                Log.e(TAG, "queryPurchases: $it")
                onPremiumStateUpdated(PremiumState.Error(it))
            })
    }

    override suspend fun buyPremiumOffer(
        activity: Activity,
        offerId: String,
        purchasedToken: String,
        onDone: (Boolean) -> Unit
    ) {
        Log.d(TAG, "buyPremiumOffer: $offerId")
        if (!connectivity.isConnected) {
            Log.w(TAG, "buyPremiumOffer: no internet, aborting fast")
            onDone(false)
            return
        }
        val premiumPackageDetails = loadedProducts[offerId] ?: loadedSubscriptions[offerId]
        if (premiumPackageDetails == null) {
            Log.e(TAG, "buyPremiumOffer: package not loaded for $offerId; query offers first")
            onDone(false)
            return
        }
        revenueCatBillingClient.purchasePremiumOffer(
            activity = activity,
            packageDetails = premiumPackageDetails,
            onPurchasesFound = {
                Log.d(TAG, "buyPremiumOffer: onPurchasesFound $it")
                onPremiumStateUpdated(PremiumState.Premium(RevenueCatSubscribedOffer(it)))
                onDone(true)
            },
            onNoPurchasesFound = {
                Log.d(TAG, "buyPremiumOffer: onNoPurchasesFound")
                onDone(false)
            },
            onFailed = { userCanceled, error ->
                if (userCanceled) {
                    Log.d(TAG, "buyPremiumOffer: user canceled")
                } else {
                    Log.e(TAG, "buyPremiumOffer: $error")
                }
                onDone(false)
            })
    }

    override suspend fun queryPremiumProducts(productIds: List<String>): List<PremiumOffer> {
        val filteredIds = productIds.filter { it.isNotEmpty() }
        if (filteredIds.isEmpty()) {
            Log.d(TAG, "queryPremiumProducts: No non-null ids provided")
            throw IllegalArgumentException("No non-null ids provided")
        }
        connectivity.awaitConnected()
        val packageDetails = retry(maxAttempts = 5, isRetryable = isRetryable) {
            revenueCatBillingClient.queryPackageDetails(filteredIds)
        }
        packageDetails.forEach { loadedProducts[it.identifier] = it }
        Log.d(TAG, "queryPremiumProducts: total products ${packageDetails.size}")
        val premiumOffers = packageDetails.map { it.toInApp() }
        premiumOffers.forEach {
            Log.d(TAG, "queryPremiumProducts: $it")
        }
        return premiumOffers
    }

    override suspend fun queryPremiumSubscriptions(subscriptionIds: List<String>): List<PremiumOffer> {
        val filteredIds = subscriptionIds.filter { it.isNotEmpty() }
        if (filteredIds.isEmpty()) {
            Log.d(TAG, "queryPremiumSubscriptions: No non-null ids provided")
            throw IllegalArgumentException("No non-null ids provided")
        }
        connectivity.awaitConnected()
        val premiumSubscription = retry(maxAttempts = 5, isRetryable = isRetryable) {
            revenueCatBillingClient.queryPackageDetails(filteredIds)
        }
        premiumSubscription.forEach { loadedSubscriptions[it.identifier] = it }
        Log.d(TAG, "queryPremiumSubscriptions: total subscriptions ${premiumSubscription.size}")
        val premiumOffers = premiumSubscription.mapNotNull { it.toSubscription() }
        premiumOffers.forEach {
            Log.d(TAG, "queryPremiumSubscriptions: $it")
        }
        return premiumOffers
    }


    private fun onPremiumStateUpdated(premiumState: PremiumState) {
        coroutineScope.launch {
            premiumStateChannel.update { current ->
                // Never downgrade a confirmed Premium on a non-confirming signal (transient error / offline / pre-check).
                if (current is PremiumState.Premium &&
                    (premiumState is PremiumState.Error ||
                            premiumState is PremiumState.Unknown ||
                            premiumState is PremiumState.Checking)
                ) {
                    current
                } else {
                    premiumState
                }
            }
        }
    }
}