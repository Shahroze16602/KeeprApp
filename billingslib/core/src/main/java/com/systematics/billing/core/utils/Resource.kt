package com.systematics.billing.core.utils

sealed class ResultResource<T>(
    val data: T? = null,
    val error: String? = null
) {

    class Idle<T> : ResultResource<T>()
    class Loading<T> : ResultResource<T>()
    class Error<T>(error: String) : ResultResource<T>(error = error)
    class Success<T>(data: T) : ResultResource<T>(data)
}