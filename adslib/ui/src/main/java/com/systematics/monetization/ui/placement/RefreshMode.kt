package com.systematics.monetization.ui.placement

import androidx.annotation.Keep
import com.systematics.monetization.ui.placement.models.RefreshModel
import kotlinx.serialization.Serializable

@Keep
@Serializable
sealed interface RefreshMode {

    @Keep
    @Serializable
    data object None : RefreshMode

    @Keep
    @Serializable
    data class RefreshOnAction(
        val refreshModel: RefreshModel,
        val oneTime: Boolean = true
    ) : RefreshMode

    @Keep
    @Serializable
    data class RefreshOnResume(
        val refreshModel: RefreshModel,
        val oneTime: Boolean = true
    ) : RefreshMode

    @Keep
    @Serializable
    data class RefreshOnInterval(
        val refreshModel: RefreshModel,
        val oneTime: Boolean = true,
        val intervalSeconds: Int
    ) : RefreshMode
}