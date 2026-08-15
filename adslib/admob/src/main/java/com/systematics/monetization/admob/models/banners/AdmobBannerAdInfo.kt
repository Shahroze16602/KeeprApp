package com.systematics.monetization.admob.models.banners

import androidx.annotation.Keep
import com.systematics.monetization.core.models.banner.BannerAdInfo
import kotlinx.serialization.Serializable

@Keep
@Serializable
abstract class AdmobBannerAdInfo() : BannerAdInfo()

@Keep
@Serializable
data class AdmobSimpleBannerAdInfo(
    override val id: String,
    override val tag: String = "",
    val isCollapsibleTop: Boolean = false,
    val isCollapsibleBottom: Boolean = false,
) : AdmobBannerAdInfo()

@Keep
@Serializable
data class AdmobAdaptiveBannerAdInfo(
    override val id: String,
    override val tag: String = "",
    val isCollapsibleTop: Boolean = false,
    val isCollapsibleBottom: Boolean = false,
) : AdmobBannerAdInfo()

@Keep
@Serializable
data class AdmobInlineAdaptiveBannerAdInfo(
    override val id: String,
    override val tag: String = "",
    val heightDp: Long
) : AdmobBannerAdInfo()

@Keep
@Serializable
data class AdmobRectangularBannerAdInfo(
    override val id: String,
    override val tag: String = ""
) : AdmobBannerAdInfo()