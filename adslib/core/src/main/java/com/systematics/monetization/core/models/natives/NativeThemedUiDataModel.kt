package com.systematics.monetization.core.models.natives

import android.content.Context
import androidx.annotation.Keep
import com.systematics.monetization.core.utils.isDarkMode
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class NativeThemedUiDataModel(
    val light: NativeUiDataModel = NativeUiDataModel(),
    val dark: NativeUiDataModel = NativeUiDataModel(
        textPrimaryColor = "#ffffff",
        textSecondaryColor = "#c7c7c7",
        ctaTextColor = "#ffffff",
        ctaBgColor = "#a83232",
        bgColor = "#070707",
        shimmerColor = "#050505",
    )
) {

    fun themed(isDark: Boolean): NativeUiDataModel {
        return if (isDark) dark else light
    }

    fun themed(context: Context): NativeUiDataModel = themed(context.isDarkMode())
}