package com.systematics.monetization.core.models.natives

data class AppRefreshableNativeAd(
    val appNativeAd: AppNativeAd,
    val refreshCount: Int
)