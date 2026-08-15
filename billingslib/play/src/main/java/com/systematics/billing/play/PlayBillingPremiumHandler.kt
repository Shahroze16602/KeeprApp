package com.systematics.billing.play

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.systematics.billing.core.connectivity.AndroidConnectivityProvider
import com.systematics.billing.core.data.datasource.PremiumHandler
import com.systematics.billing.core.domain.connectivity.ConnectivityProvider
import com.systematics.billing.core.domain.model.PremiumOffer
import com.systematics.billing.core.domain.model.PremiumState
import com.systematics.billing.play.models.PlaySubscribedOffer
import com.systematics.billing.play.utils.isOk
import com.systematics.billing.play.utils.isPurchased
import com.systematics.billing.play.utils.toInApp
import com.systematics.billing.play.utils.toSubscription
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayBillingPremiumHandler(
    context: Context,
    private val connectivity: ConnectivityProvider = AndroidConnectivityProvider(context),
    config: BillingResilienceConfig = BillingResilienceConfig()
) : PremiumHandler, PurchasesUpdatedListener {

    override val premiumStateChannel: MutableStateFlow<PremiumState> =
        MutableStateFlow(PremiumState.Unknown)

    private val playBillingClient = PlayBillingClient(context, this, connectivity, config)
    private val loadedProducts: HashMap<String, ProductDetails> = hashMapOf()
    private val loadedSubscriptions: HashMap<String, ProductDetails> = hashMapOf()

    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var onPurchaseFlowDone: ((Boolean) -> Unit)? = null

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        Log.d(TAG, "onPurchasesUpdated: updated ${billingResult.responseCode}")
        val isOk = billingResult.isOk()
        if (isOk) {
            Log.d(TAG, "onPurchasesUpdated: purchase OK")
            if (!purchases.isNullOrEmpty()) {
                Log.d(TAG, "onPurchasesUpdated: purchases found")
                onPurchasesFound(purchases)
            } else {
                Log.d(TAG, "onPurchasesUpdated: no purchases found")
                onNoPurchasesFound()
                onPurchaseFlowDone?.invoke(false)
                onPurchaseFlowDone = null
            }
        } else {
            if (premiumStateChannel.value !is PremiumState.Premium && premiumStateChannel.value != PremiumState.NonPremium) {
                onPremiumStateUpdated(PremiumState.Unknown)
            }
            Log.d(
                TAG,
                "onPurchasesUpdated: purchase failed with code ${billingResult.responseCode}"
            )
            onPurchaseFlowDone?.invoke(false)
            onPurchaseFlowDone = null
        }
    }

    override suspend fun queryPurchases(entitlementIds: List<String>) {
        Log.d(TAG, "queryPurchases:")
        playBillingClient.queryPurchases(
            onPurchasesFound = {
                Log.d(TAG, "queryPurchases: onPurchasesFound $it")
                onPurchasesFound(it)
            },
            onNoPurchasesFound = {
                Log.d(TAG, "queryPurchases: onNoPurchasesFound")
                onNoPurchasesFound()
            },
            onFailed = {
                Log.e(TAG, "queryPurchases: ", it)
                onPremiumStateUpdated(PremiumState.Error(it.message ?: "queryPurchases failed"))
            }
        )
    }

    override suspend fun buyPremiumOffer(
        activity: Activity,
        offerId: String,
        purchasedToken: String,
        onDone: (Boolean) -> Unit,
    ) {
        Log.d(TAG, "buyPremiumOffer: $offerId")
        if (!connectivity.isConnected) {
            Log.w(TAG, "buyPremiumOffer: no internet, aborting fast")
            onDone(false)
            return
        }
        val premiumProductDetails = loadedProducts[offerId] ?: loadedSubscriptions[offerId]
        if (premiumProductDetails == null) {
            Log.e(TAG, "buyPremiumOffer: details not loaded for $offerId; query offers first")
            onDone(false)
            return
        }
        try {
            // Register the callback before launching so a fast onPurchasesUpdated can't race it.
            this.onPurchaseFlowDone = onDone
            val purchaseResult = playBillingClient.purchasePremiumOffer(
                activity,
                premiumProductDetails,
                purchasedToken
            )
            Log.d(TAG, "buyPremiumOffer: purchase result ${purchaseResult.responseCode}")
            if (!purchaseResult.isOk()) {
                // Launch failed: onPurchasesUpdated won't fire, so resolve the callback here.
                Log.e(TAG, "buyPremiumOffer: launch failed ${purchaseResult.responseCode}")
                onPurchaseFlowDone?.invoke(false)
                onPurchaseFlowDone = null
            }
        } catch (ex: Exception) {
            Log.e(TAG, "buyPremiumOffer: ", ex)
            onPurchaseFlowDone = null
            onDone(false)
        }
    }

    override suspend fun queryPremiumProducts(productIds: List<String>): List<PremiumOffer> {
        val filteredIds = productIds.filter { it.isNotEmpty() }
        if (filteredIds.isEmpty()) {
            Log.d(TAG, "queryPremiumProducts: No non-null ids provided")
            throw IllegalArgumentException("No non-null ids provided")
        }
        val premiumProducts = playBillingClient.queryProducts(filteredIds)
        premiumProducts.forEach { loadedProducts[it.productId] = it }
        Log.d(TAG, "queryPremiumProducts: total products ${premiumProducts.size}")
        val premiumOffers = premiumProducts.mapNotNull { it.toInApp() }
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
        val premiumSubscription = playBillingClient.querySubscriptions(filteredIds)
        premiumSubscription.forEach { loadedSubscriptions[it.productId] = it }
        Log.d(TAG, "queryPremiumSubscriptions: total subscriptions ${premiumSubscription.size}")
        val premiumOffers = premiumSubscription.mapNotNull { it.toSubscription() }
        premiumOffers.forEach {
            Log.d(TAG, "queryPremiumSubscriptions: $it")
        }
        return premiumOffers
    }

    private fun onNoPurchasesFound() {
        Log.d(TAG, "onNoPurchasesFound: ")
        onPremiumStateUpdated(PremiumState.NonPremium)
    }

    private fun getSku(skuList: MutableList<String>): String {
        return if (skuList.isNotEmpty()) {
            skuList[0]
        } else ""
    }

    private fun onPurchasesFound(purchases: List<Purchase>) {
        Log.d(TAG, "onPurchasesFound: ${purchases.size}")
        val purchasedItems = mutableListOf<Purchase>()
        purchases.forEach {
            if (it.isPurchased()) {
                Log.d(TAG, "onPurchasesFound id: ${getSku(it.products)}")
                purchasedItems.add(it)
                if (!it.isAcknowledged) {
                    Log.d(TAG, "onPurchasesFound: acknowledging purchase $it")
                    coroutineScope.launch {
                        try {
                            playBillingClient.acknowledgePurchase(it)
                            Log.d(TAG, "onPurchasesFound: acknowledged $it")
                        } catch (ex: Exception) {
                            Log.e(TAG, "onPurchasesFound: ", ex)
                        }
                    }
                } else {
                    Log.d(TAG, "onPurchasesFound: ${getSku(it.products)} already acknowledge")
                }
            }
        }
        if (purchasedItems.isNotEmpty()) {
            onPremiumStateUpdated(
                PremiumState.Premium(PlaySubscribedOffer(purchasedItems))
            )
            onPurchaseFlowDone?.invoke(true)
        } else {
            onPurchaseFlowDone?.invoke(false)
        }
        onPurchaseFlowDone = null
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

    companion object {

        private const val TAG = "PlayBillingPremiumHandlerTAG"
    }
}
