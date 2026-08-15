package com.systematics.monetization.ui.placement.models

import androidx.annotation.Keep
import com.systematics.monetization.core.models.banner.BannerAdInfo
import com.systematics.monetization.ui.placement.models.abs.InlinePlacementModel
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class BannerPlacementModel(
    override val isEnabled: Boolean = true,
    override val tag: String = "",

    val bannerAdInfo: BannerAdInfo,
    val preserved: Boolean = true
) : InlinePlacementModel()