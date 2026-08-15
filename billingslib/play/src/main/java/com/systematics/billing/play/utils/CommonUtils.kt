package com.systematics.billing.play.utils

import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.systematics.billing.core.utils.BillingException
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "CommonUtilsTAG"

/**
 * Establishes the billing connection. Resumes with a [BillingException] (carrying the
 * response code) on failure so callers can `try/catch` and retry — unlike cancelling the
 * continuation, which would kill the calling coroutine. Guards against the SDK invoking
 * the listener more than once.
 */
suspend fun BillingClient.init(): Boolean = suspendCancellableCoroutine { continuation ->
    val resumed = AtomicBoolean(false)
    startConnection(object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() {
            if (resumed.compareAndSet(false, true)) {
                continuation.resumeWithException(
                    BillingException(BillingResponseCode.SERVICE_DISCONNECTED, "Service Disconnected")
                )
            }
        }

        override fun onBillingSetupFinished(billingResult: BillingResult) {
            if (!resumed.compareAndSet(false, true)) return
            if (billingResult.isOk()) {
                Log.d(TAG, "onBillingSetupFinished: initialized")
                continuation.resume(true)
            } else {
                Log.d(TAG, "onBillingSetupFinished: ${billingResult.responseCode}")
                continuation.resumeWithException(
                    BillingException(
                        billingResult.responseCode,
                        "Service setup failed with response ${billingResult.responseCode}"
                    )
                )
            }
        }
    })
}

fun Purchase.isPurchased() = this.purchaseState == Purchase.PurchaseState.PURCHASED

fun BillingResult.isOk() = this.responseCode == BillingResponseCode.OK

fun BillingResult.toException() = Exception("$responseCode")

fun getSku(skuList: MutableList<String>): String {
    return if (skuList.isNotEmpty()) {
        skuList[0]
    } else ""
}