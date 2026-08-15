package com.systematics.billing.play

/**
 * Tunable retry/backoff knobs for the resilient billing client.
 *
 * - [restoreMaxAttempts] is effectively unbounded so the app-launch entitlement/restore
 *   check keeps retrying across connectivity outages until it resolves.
 * - [offersMaxAttempts] / [acknowledgeMaxAttempts] are bounded so the UI can eventually
 *   surface an error + retry rather than spinning forever once internet is present.
 */
data class BillingResilienceConfig(
    val connectMaxAttempts: Int = 5,
    val restoreMaxAttempts: Int = Int.MAX_VALUE,
    val offersMaxAttempts: Int = 5,
    val acknowledgeMaxAttempts: Int = 5,
    val initialDelayMs: Long = 500,
    val maxDelayMs: Long = 8_000,
)
