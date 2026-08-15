package com.systematics.monetization.ui.breakpoints

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class BreakPointModel(
    val adgroupBreakPoints: List<AdGroupBreakPointModel> = listOf(),
    val timersToStart: List<TimerBreakPointModel> = listOf()
)