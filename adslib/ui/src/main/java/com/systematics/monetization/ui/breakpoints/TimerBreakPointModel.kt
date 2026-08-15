package com.systematics.monetization.ui.breakpoints

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class TimerBreakPointModel(
    val timer: String,
    val isReached: Boolean = false
)
