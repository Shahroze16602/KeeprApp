package com.systematics.monetization.ui.breakpoints

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class AdGroupBreakPointModel(
    val adGroupType: String,
    val placementKeys: List<String> = listOf()
)