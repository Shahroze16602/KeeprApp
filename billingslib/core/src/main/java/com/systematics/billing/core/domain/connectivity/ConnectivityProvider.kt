package com.systematics.billing.core.domain.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over device internet connectivity used by the billing layer so the
 * library can wait for connectivity before talking to the billing backend.
 *
 * Kept in core (platform-agnostic interface); the concrete [AndroidConnectivityProvider]
 * lives alongside it and uses Android's ConnectivityManager.
 */
interface ConnectivityProvider {

    /** True if the device currently has validated internet access. */
    val isConnected: Boolean

    /** Emits the current connectivity and every subsequent change. */
    fun observe(): Flow<Boolean>

    /**
     * Suspends until the device has internet. Returns immediately if already connected.
     * Has no timeout by design — the caller's coroutine scope (app scope / viewModelScope)
     * bounds how long the wait lives.
     */
    suspend fun awaitConnected()
}
