package com.systematics.monetization.core.utils

sealed interface StatusCode {

    data object Success : StatusCode
    data object Timeout : StatusCode
    data class Failed(val exception: Throwable) : StatusCode
}