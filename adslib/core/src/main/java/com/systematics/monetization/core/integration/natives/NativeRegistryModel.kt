package com.systematics.monetization.core.integration.natives

import com.systematics.monetization.core.models.natives.AppNativeAd
import kotlin.reflect.KClass

data class NativeRegistryModel<T : AppNativeAd>(
    val registryClass: KClass<T>,
    val registryMap: Map<String, NativeLayoutRegistry<T>>
)