package com.systematics.monetization.core.utils

import com.systematics.monetization.core.integration.natives.NativeLayoutRegistry
import com.systematics.monetization.core.integration.natives.NativeRegistryModel
import com.systematics.monetization.core.models.natives.AppNativeAd

fun <T : AppNativeAd> NativeRegistryModel<T>.addRegistry(
    key: String,
    registry: NativeLayoutRegistry<T>
): NativeRegistryModel<T> {
    val registryMap = mapOf(key to registry)
    return addRegistries(registryMap)
}

fun <T : AppNativeAd> NativeRegistryModel<T>.addRegistries(
    registries: Map<String, NativeLayoutRegistry<T>>
): NativeRegistryModel<T> {
    val currentMap = registryMap.toMutableMap()
    currentMap.putAll(registries)
    return this.copy(registryMap = currentMap)
}