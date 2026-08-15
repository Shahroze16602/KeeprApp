package com.systematics.monetization.core.models.natives

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class NativeUiDataModel(
    val textPrimaryColor: String = "#000000",
    val textSecondaryColor: String = "#141414",
    val ctaTextColor: String = "#ffffff",
    val ctaBgColor: String = "#a83232",
    val bgColor: String = "#fff7f7",
    val shimmerColor: String = "#e0e0e0"
)