package com.systematics.monetization.ui.models

import androidx.annotation.Keep
import com.systematics.monetization.core.models.ad.remote.RemoteAdInfo
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class ConditionalModel(
    val input: RemoteAdInfo = RemoteAdInfo(),
    val output: RemoteAdInfo = RemoteAdInfo()
)