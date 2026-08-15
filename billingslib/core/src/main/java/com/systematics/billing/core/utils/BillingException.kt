package com.systematics.billing.core.utils

/**
 * Failure raised by the billing layer. Carries the provider response code (when known)
 * so [retry] can classify the failure as retryable or terminal without depending on
 * any provider SDK types in core.
 */
class BillingException(
    val responseCode: Int? = null,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
