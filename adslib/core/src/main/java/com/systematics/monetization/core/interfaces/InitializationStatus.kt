package com.systematics.monetization.core.interfaces

sealed interface InitializationStatus {

    data object Uninitialized : InitializationStatus
    data object Initializing : InitializationStatus
    data object Deferred : InitializationStatus
    data object Success : InitializationStatus
    data object Failed : InitializationStatus
}