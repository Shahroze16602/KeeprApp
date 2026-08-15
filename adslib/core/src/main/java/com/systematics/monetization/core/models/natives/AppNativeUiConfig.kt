package com.systematics.monetization.core.models.natives

import kotlinx.serialization.Serializable

@Serializable
data class AppNativeUiConfig(
    val defaultNativeTheme: NativeThemedUiDataModel = NativeThemedUiDataModel(),
    val nativeThemedPlacements: Map<String, NativeThemedUiDataModel> = emptyMap()
)