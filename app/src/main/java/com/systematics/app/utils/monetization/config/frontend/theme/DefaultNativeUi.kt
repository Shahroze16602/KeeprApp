package com.systematics.app.utils.monetization.config.frontend.theme

import com.systematics.monetization.core.models.natives.AppNativeUiConfig
import com.systematics.monetization.core.models.natives.NativeThemedUiDataModel
import com.systematics.monetization.core.models.natives.NativeUiDataModel

private val appNativeTheme = NativeThemedUiDataModel(
    light = NativeUiDataModel(
        textPrimaryColor = "#000000",
        textSecondaryColor = "#5F5A52",
        ctaTextColor = "#FFFFFF",
        ctaBgColor = "#1D6B52",
        bgColor = "#F7F7F5",
        shimmerColor = "#E4F0EB"
    ),
    dark = NativeUiDataModel(
        textPrimaryColor = "#FFFFFF",
        textSecondaryColor = "#C7C7C7",
        ctaTextColor = "#FFFFFF",
        ctaBgColor = "#1D6B52",
        bgColor = "#0E0E0E",
        shimmerColor = "#1A1A1A"
    )
)

val defaultAppNativeUiConfig = AppNativeUiConfig(
    defaultNativeTheme = appNativeTheme,
    nativeThemedPlacements = mapOf()
)
