package com.systematics.keepr.utils.core

import androidx.annotation.StringRes
import com.systematics.keepr.R

sealed class ResultResource<T>(
    val data: T? = null,
    val error: String? = null,
    @param:StringRes val message: Int = R.string.space_,
) {
    class Idle<T> : ResultResource<T>()
    class Loading<T> : ResultResource<T>()
    class ResError<T>(@StringRes message: Int) : ResultResource<T>(message = message)
    class Error<T>(error: String) : ResultResource<T>(error = error)
    class Success<T>(data: T) : ResultResource<T>(data)
}
