package com.systematics.monetization.core.interfaces

import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.models.AdManagerResult
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.models.ad.local.group.abs.AdInfoGroup
import kotlinx.coroutines.CoroutineScope

interface IAdGroupLoadManager {

    val managerScope: CoroutineScope
    val adInfoGroup: AdInfoGroup

    var adLoadListener: AdManager.AdLoadListener?
    val isRepeatable: Boolean
    val timedDebounce: Boolean

    val preserveWhileWorking: Boolean
    val parkOnShown: Boolean

    fun loadAds()

    fun isAdLoaded(): Boolean

    fun getLoadedAdsCount(): Int {
        return if (isAdLoaded()) {
            1
        } else {
            0
        }
    }

    fun isManualAdLoaded(): Boolean

    fun isAdLoading(): Boolean

    fun getLoadedAdManager(): AdManagerResult?

    fun removeThisAd(adInfo: AdInfo)

    fun destroy()
}