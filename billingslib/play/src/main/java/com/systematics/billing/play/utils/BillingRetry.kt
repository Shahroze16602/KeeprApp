package com.systematics.billing.play.utils

import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingResult
import com.systematics.billing.core.utils.BillingException

/**
 * Classifies Google Play [BillingResponseCode]s and throwables as retryable (transient)
 * or terminal, and converts non-OK [BillingResult]s into [BillingException]s carrying the code.
 */

fun isRetryableResponseCode(code: Int?): Boolean = when (code) {
    BillingResponseCode.SERVICE_DISCONNECTED,
    BillingResponseCode.SERVICE_UNAVAILABLE,
    BillingResponseCode.NETWORK_ERROR,
    BillingResponseCode.ERROR -> true

    // Terminal — retrying wastes time and hides real config/state bugs.
    BillingResponseCode.BILLING_UNAVAILABLE,
    BillingResponseCode.FEATURE_NOT_SUPPORTED,
    BillingResponseCode.ITEM_UNAVAILABLE,
    BillingResponseCode.ITEM_ALREADY_OWNED,
    BillingResponseCode.ITEM_NOT_OWNED,
    BillingResponseCode.DEVELOPER_ERROR,
    BillingResponseCode.USER_CANCELED -> false

    null -> true // unknown — give it a chance
    else -> false
}

/** Decides whether a throwable thrown by the billing layer should be retried. */
fun Throwable.isRetryableBillingError(): Boolean = when (this) {
    is BillingException -> isRetryableResponseCode(responseCode)
    // Programming/precondition errors must never loop.
    is IllegalArgumentException, is IllegalStateException -> false
    // IO / unexpected transient failures — retry within the attempt cap.
    else -> true
}

/** Throws a [BillingException] carrying the response code if this result is not OK. */
fun BillingResult.throwIfNotOk(context: String) {
    if (!isOk()) {
        throw BillingException(responseCode, "$context failed: code=$responseCode, msg=$debugMessage")
    }
}
