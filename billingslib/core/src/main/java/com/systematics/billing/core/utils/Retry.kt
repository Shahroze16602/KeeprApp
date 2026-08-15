package com.systematics.billing.core.utils

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.random.Random

/**
 * Runs [block] and retries it on retryable failures with exponential backoff + jitter.
 *
 * - [CancellationException] is always rethrown immediately (structured-concurrency safe;
 *   a cancelled [kotlinx.coroutines.flow.collectLatest] / scope won't be misread as a billing error).
 * - A failure for which [isRetryable] returns false is rethrown immediately (terminal error).
 * - After [maxAttempts] retryable failures the last error is rethrown.
 *
 * @param maxAttempts total attempts including the first. Use a large value for operations
 *   that must keep trying across transient outages.
 * @param block receives the zero-based attempt index.
 */
suspend fun <T> retry(
    maxAttempts: Int = 5,
    initialDelayMs: Long = 500,
    maxDelayMs: Long = 8_000,
    factor: Double = 2.0,
    isRetryable: (Throwable) -> Boolean,
    block: suspend (attempt: Int) -> T
): T {
    var lastError: Throwable? = null
    var attempt = 0
    while (attempt < maxAttempts) {
        try {
            return block(attempt)
        } catch (ce: CancellationException) {
            throw ce
        } catch (t: Throwable) {
            lastError = t
            if (!isRetryable(t) || attempt == maxAttempts - 1) throw t
            val backoff = (initialDelayMs * factor.pow(attempt))
                .toLong()
                .coerceIn(initialDelayMs, maxDelayMs)
            val jitter = Random.nextLong(0, backoff / 2 + 1)
            delay(backoff + jitter)
            attempt++
        }
    }
    throw lastError ?: IllegalStateException("retry: exhausted with no recorded error")
}
