package com.systematics.monetization.ui.placement.models

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class BackupModel(
    val adGroupType: String,
    val isInstantAllowed: Boolean = false
)