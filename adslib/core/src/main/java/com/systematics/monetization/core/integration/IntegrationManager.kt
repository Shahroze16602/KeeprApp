package com.systematics.monetization.core.integration

import android.content.Context
import android.util.Log
import com.systematics.monetization.core.integration.interfaces.AdsNetworkIntegration
import com.systematics.monetization.core.integration.natives.NativeLayoutRegistry
import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.managers.populate.BannerPopulateManager
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.models.banner.BannerAdInfo
import com.systematics.monetization.core.models.natives.AppNativeAd
import kotlinx.serialization.modules.SerializersModule

class IntegrationManager(
    private val context: Context,
    private val mapAdInfo: (AdInfo) -> AdInfo = { it },
    private val allIntegrations: List<AdsNetworkIntegration>
) {

    val supportedAdTypes: () -> List<String> = {
        allIntegrations.flatMap { it.adTypes }
    }

    fun <T : AppNativeAd> getNativeRegistry(
        nativeAd: T,
        viewType: String
    ): NativeLayoutRegistry<T>? {
        allIntegrations.forEach { integration ->
            val registry = integration.nativeRegistry
            if (registry.registryClass == nativeAd::class) {
                Log.d(TAG, "getNativeRegistry: registry class found as $registry")
                if (registry.registryMap.contains(viewType)) {
                    Log.d(TAG, "getNativeRegistry: view type also found")
                    return registry.registryMap[viewType] as NativeLayoutRegistry<T>?
                }
            }
        }
        return null
    }

    fun getNativeRegistryForView(viewType: String): NativeLayoutRegistry<*>? {
        if (allIntegrations.isNotEmpty()) {
            val registry = allIntegrations.first().nativeRegistry
            if (registry.registryMap.contains(viewType)) {
                Log.d(TAG, "getNativeRegistry: view type also found")
                return registry.registryMap[viewType]
            }
        }
        return null
    }

    suspend fun <T : BannerAdInfo> getBannerPopulateManager(
        bannerAdInfo: BannerAdInfo
    ): BannerPopulateManager<in T>? {
        allIntegrations.forEach { integration ->
            val populateManager = integration.bannerPopulateManager
            if (populateManager.canHandle(bannerAdInfo)) {
                if (!integration.isReady()) {
                    integration.initialize()
                }
                return populateManager as BannerPopulateManager<in T>
            }
        }
        return null
    }

    fun getBannerSerializable(): List<SerializersModule> {
        return allIntegrations.map { it.bannerPopulateManager.bannerSerializableModule }
    }

    fun mapTestIds(adType: String, adUnitId: String): String {
        allIntegrations.forEach {
            if (it.adTypes.contains(adType)) {
                return it.mapTestIds(adType, adUnitId)
            }
        }
        return adType
    }

    suspend fun createAdManager(adInfo: AdInfo): AdManager<*> {
        val mappedAdInfo = mapAdInfo(adInfo)
        if (mappedAdInfo != adInfo) {
            Log.d(TAG, "createAdManager: mapped $mappedAdInfo")
        }
        allIntegrations.forEach {
            if (it.adTypes.contains(mappedAdInfo.adType)) {
                return it.createAdManager(mappedAdInfo)
            }
        }
        throw IllegalArgumentException("Unknown ad type: ${mappedAdInfo.adType}")
    }

    companion object {

        private const val TAG = "IntegrationManagerTAG"
    }
}