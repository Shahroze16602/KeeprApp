package com.systematics.monetization.ui.placement.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class RefreshModel(
    val adGroupType: String = "",
    val isInstantAllowed: Boolean = true
)