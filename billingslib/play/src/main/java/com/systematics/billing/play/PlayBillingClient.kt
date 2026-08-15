package com.systematics.billing.play

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryPurchasesAsync
import com.systematics.billing.core.domain.connectivity.ConnectivityProvider
import com.systematics.billing.core.utils.BillingException
import com.systematics.billing.core.utils.retry
import com.systematics.billing.play.utils.init
import com.systematics.billing.play.utils.isOk
import com.systematics.billing.play.utils.isRetryableBillingError
import com.systematics.billing.play.utils.throwIfNotOk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlayBillingClient(
    context: Context,
    purchasesUpdatedListener: PurchasesUpdatedListener,
    private val connectivity: ConnectivityProvider,
    private val config: BillingResilienceConfig = BillingResilienceConfig()
) {
    private val billingClient: BillingClient by lazy {
        BillingClient.newBuilder(context)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder().enableOneTimeProducts().enablePrepaidPlans()
                    .build()
            )
            .enableAutoServiceReconnection()
            .setListener(purchasesUpdatedListener).build()
    }
    private val initializationMutex = Mutex()

    suspend fun queryPurchases(
        onPurchasesFound: (List<Purchase>) -> Unit,
        onNoPurchasesFound: () -> Unit,
        onFailed: (Exception) -> Unit
    ) {
        Log.d(TAG, "queryPurchases: Starting combined query")
        try {
            // Entitlement restore must eventually succeed: keep retrying transient failures,
            // re-waiting for internet each attempt, until purchases resolve or a terminal error.
            val allPurchases = retry(
                maxAttempts = config.restoreMaxAttempts,
                initialDelayMs = config.initialDelayMs,
                maxDelayMs = config.maxDelayMs,
                isRetryable = { it.isRetryableBillingError() }
            ) {
                awaitReadyClient()

                val subsResult = billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
                subsResult.billingResult.throwIfNotOk("queryPurchases(SUBS)")

                val inAppResult = billingClient.queryPurchasesAsync(
                    QueryPurchasesParams.newBuilder()
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
                inAppResult.billingResult.throwIfNotOk("queryPurchases(INAPP)")

                subsResult.purchasesList + inAppResult.purchasesList
            }

            if (allPurchases.isNotEmpty()) {
                Log.d(TAG, "queryPurchases: Found ${allPurchases.size} total items")
                onPurchasesFound(allPurchases)
            } else {
                Log.d(TAG, "queryPurchases: No items found in either category")
                onNoPurchasesFound()
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.e(TAG, "queryPurchases: failed after retries", e)
            onFailed(e)
        }
    }

    suspend fun purchasePremiumOffer(
        activity: Activity,
        productDetails: ProductDetails,
        purchasedToken: String?
    ): BillingResult {
        Log.d(TAG, "purchasePremiumOffer: start")
        awaitReadyClient()

        if (activity.isFinishing || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed)) {
            Log.w(TAG, "purchasePremiumOffer: activity not in a valid state to launch billing flow")
            throw IllegalStateException("Activity not in a valid state to launch billing flow")
        }

        // Build ProductDetailsParams safely: subscriptions require an offerToken; in-app products must NOT set one.
        val paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        val hasSubsOffers = !productDetails.subscriptionOfferDetails.isNullOrEmpty()
        val hasOneTimeOffer = !productDetails.oneTimePurchaseOfferDetailsList.isNullOrEmpty()
        if (hasSubsOffers) {
            val offerToken = productDetails.subscriptionOfferDetails!!.firstOrNull()?.offerToken
            if (offerToken.isNullOrBlank()) {
                Log.e(
                    TAG,
                    "purchasePremiumOffer: missing subscription offerToken; aborting launch to avoid crash"
                )
                throw IllegalStateException("missing subscription offerToken")
            }
            paramsBuilder.setOfferToken(offerToken)
        } else if (hasOneTimeOffer) {
            val offerToken =
                productDetails.oneTimePurchaseOfferDetailsList!!.firstOrNull()?.offerToken
            if (offerToken.isNullOrBlank()) {
                Log.e(
                    TAG,
                    "purchasePremiumOffer: missing in app offerToken; aborting launch to avoid crash"
                )
                throw IllegalStateException("missing in app offerToken")
            }
            paramsBuilder.setOfferToken(offerToken)
        }

        val productDetailsParamsList = listOf(paramsBuilder.build())

        val billingFlowParams = if (hasSubsOffers && !purchasedToken.isNullOrBlank()) {
            Log.d(TAG, "purchasePremiumOffer: subscription update with token")
            val subscriptionUpdateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(purchasedToken)
                .setSubscriptionReplacementMode(
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITH_TIME_PRORATION
                )
                .build()

            BillingFlowParams.newBuilder()
                .setSubscriptionUpdateParams(subscriptionUpdateParams)
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
        } else {
            Log.d(TAG, "purchasePremiumOffer: new purchase flow")
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()
        }

        val result = billingClient.launchBillingFlow(activity, billingFlowParams)

        Log.d(
            TAG,
            "purchasePremiumOffer: launch result code=${result.responseCode} msg=${result.debugMessage}"
        )
        return result
    }

    suspend fun acknowledgePurchase(purchase: Purchase) {
        // Acknowledge is idempotent by purchase token, so retrying after a transient disconnect is safe.
        retry(
            maxAttempts = config.acknowledgeMaxAttempts,
            initialDelayMs = config.initialDelayMs,
            maxDelayMs = config.maxDelayMs,
            isRetryable = { it.isRetryableBillingError() }
        ) {
            awaitReadyClient()
            suspendCancellableCoroutine { continuation ->
                val acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken)
                        .build()
                if (billingClient.isReady) {
                    billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
                        if (it.isOk()) {
                            Log.d(TAG, "acknowledgePurchase: purchase acknowledge success")
                            continuation.resume(Unit)
                        } else {
                            Log.d(TAG, "acknowledgePurchase: failed to acknowledge ${it.responseCode}")
                            continuation.resumeWithException(
                                BillingException(it.responseCode, "Failed to acknowledge: ${it.responseCode}")
                            )
                        }
                    }
                } else {
                    continuation.resumeWithException(
                        BillingException(
                            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
                            "Billing client disconnected"
                        )
                    )
                }
            }
        }
    }

    // Product Details
    suspend fun queryProducts(productList: List<String>): List<ProductDetails> =
        queryProductOfType(productList, BillingClient.ProductType.INAPP)

    suspend fun querySubscriptions(productList: List<String>): List<ProductDetails> =
        queryProductOfType(productList, BillingClient.ProductType.SUBS)

    private suspend fun queryProductOfType(
        productList: List<String>,
        productType: String
    ): List<ProductDetails> {
        Log.d(TAG, "queryProductOfType: $productType")
        val productsQueryList = productList.map {
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(it)
                .setProductType(productType)
                .build()
        }
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(productsQueryList)
            .build()

        // Bounded retry so the offers UI can eventually surface an error + retry once internet is present.
        return retry(
            maxAttempts = config.offersMaxAttempts,
            initialDelayMs = config.initialDelayMs,
            maxDelayMs = config.maxDelayMs,
            isRetryable = { it.isRetryableBillingError() }
        ) {
            awaitReadyClient()
            queryProductDetails(queryProductDetailsParams)
        }
    }

    private suspend fun queryProductDetails(
        params: QueryProductDetailsParams
    ): List<ProductDetails> = suspendCancellableCoroutine { continuation ->
        billingClient.queryProductDetailsAsync(params) { billingResult, result ->
            if (billingResult.isOk()) {
                continuation.resume(result.productDetailsList)
            } else {
                continuation.resumeWithException(
                    BillingException(
                        billingResult.responseCode,
                        "Failed to query products: code=${billingResult.responseCode}, msg=${billingResult.debugMessage}"
                    )
                )
            }
        }
    }

    /** Waits for internet, then ensures the billing client is connected (retrying transient connect failures). */
    private suspend fun awaitReadyClient() {
        connectivity.awaitConnected()
        initClientIfRequired()
    }

    private suspend fun initClientIfRequired() {
        if (billingClient.isReady) return
        Log.d(TAG, "initClientIfRequired: initializing billing client synced")
        initializationMutex.withLock {
            if (billingClient.isReady) return@withLock
            connectivity.awaitConnected()
            retry(
                maxAttempts = config.connectMaxAttempts,
                initialDelayMs = config.initialDelayMs,
                maxDelayMs = config.maxDelayMs,
                isRetryable = { it.isRetryableBillingError() }
            ) {
                billingClient.init()
            }
            Log.d(TAG, "initClientIfRequired: client initialized")
        }
    }

    companion object {
        private const val TAG = "PlayBillingClient"
    }
}
