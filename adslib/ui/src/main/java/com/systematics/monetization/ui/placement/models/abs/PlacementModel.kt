package com.systematics.monetization.ui.placement.models.abs

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
abstract class PlacementModel {
    abstract val isEnabled: Boolean
    abstract val tag: String
}