package com.systematics.monetization.core.models.banner

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
abstract class BannerAdInfo() {
    abstract val id: String
    abstract val tag: String
}