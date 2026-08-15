package com.systematics.monetization.core.integration.interfaces

import com.systematics.monetization.core.integration.natives.NativeRegistryModel
import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.managers.populate.BannerPopulateManager
import com.systematics.monetization.core.models.AdsNetworkState
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.revenue.interfaces.RevenueListener

interface AdsNetworkIntegration {

    val adsNetworkApp: AdsNetworkApp

    var adsNetworkState: AdsNetworkState

    val adTypes: List<String>

    val bannerPopulateManager: BannerPopulateManager<*>

    val nativeRegistry: NativeRegistryModel<*>

    suspend fun initialize(): Boolean

    suspend fun defer()

    suspend fun createAdManager(adInfo: AdInfo): AdManager<*>

    fun mapTestIds(adType: String, adUnitId: String): String

    fun attachRevenueListener(revenueListener: RevenueListener)

    fun isReady(): Boolean {
        return adsNetworkState == AdsNetworkState.Initialized
    }

    fun passConsent(consentGranted: Boolean)
}