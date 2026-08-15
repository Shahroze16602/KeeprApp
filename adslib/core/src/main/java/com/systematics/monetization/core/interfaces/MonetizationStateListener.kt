package com.systematics.monetization.core.interfaces

interface MonetizationStateListener {

    fun onInitializationStatusUpdated(status: InitializationStatus)
}