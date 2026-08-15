package com.systematics.monetization.core.managers.populate

import android.app.Activity
import com.systematics.monetization.core.models.banner.AppBannerAdView
import com.systematics.monetization.core.models.banner.BannerAdInfo
import kotlinx.serialization.modules.SerializersModule
import kotlin.reflect.KClass

abstract class BannerPopulateManager<T : BannerAdInfo> {

    abstract val supportedTypes: List<KClass<out T>>

    abstract val bannerSerializableModule: SerializersModule

    abstract fun populateBannerAdView(
        activity: Activity,
        bannerAdInfo: T,
        testIdsInDebug: Boolean = true,
        onDone: () -> Unit,
        onFailed: (Exception) -> Unit,
    ): AppBannerAdView<*>

    abstract fun getShimmerHeight(
        activity: Activity,
        bannerAdInfo: T,
    ): Int

    fun canHandle(adInfo: BannerAdInfo): Boolean {
        return supportedTypes.any { it.java.isAssignableFrom(adInfo::class.java) }
    }
}