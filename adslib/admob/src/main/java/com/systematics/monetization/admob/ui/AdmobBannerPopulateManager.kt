package com.systematics.monetization.admob.ui

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.systematics.monetization.admob.AdmobNetworkApp
import com.systematics.monetization.admob.models.banners.AdmobAdaptiveBannerAdInfo
import com.systematics.monetization.admob.models.banners.AdmobAppBannerAdView
import com.systematics.monetization.admob.models.banners.AdmobBannerAdInfo
import com.systematics.monetization.admob.models.banners.AdmobInlineAdaptiveBannerAdInfo
import com.systematics.monetization.admob.models.banners.AdmobRectangularBannerAdInfo
import com.systematics.monetization.admob.models.banners.AdmobSimpleBannerAdInfo
import com.systematics.monetization.admob.revenue.models.AdmobRevenue
import com.systematics.monetization.admob.ui.utils.adaptiveBannerAdSize
import com.systematics.monetization.admob.ui.utils.inlineAdaptiveBannerAdSize
import com.systematics.monetization.core.managers.populate.BannerPopulateManager
import com.systematics.monetization.core.models.banner.BannerAdInfo
import com.systematics.monetization.core.utils.AD_ID_TEST_BANNER
import com.systematics.monetization.core.utils.MonetizationSharedConfig
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlin.reflect.KClass

class AdmobBannerPopulateManager : BannerPopulateManager<AdmobBannerAdInfo>() {

    override val supportedTypes: List<KClass<out AdmobBannerAdInfo>> = listOf(
        AdmobSimpleBannerAdInfo::class,
        AdmobAdaptiveBannerAdInfo::class,
        AdmobInlineAdaptiveBannerAdInfo::class,
        AdmobRectangularBannerAdInfo::class
    )

    override val bannerSerializableModule: SerializersModule = SerializersModule {
        polymorphic(BannerAdInfo::class) {
            subclass(
                AdmobSimpleBannerAdInfo::class,
                AdmobSimpleBannerAdInfo.serializer()
            )
            subclass(
                AdmobAdaptiveBannerAdInfo::class,
                AdmobAdaptiveBannerAdInfo.serializer()
            )
            subclass(
                AdmobInlineAdaptiveBannerAdInfo::class,
                AdmobInlineAdaptiveBannerAdInfo.serializer()
            )
            subclass(
                AdmobRectangularBannerAdInfo::class,
                AdmobRectangularBannerAdInfo.serializer()
            )
        }
    }

    override fun populateBannerAdView(
        activity: Activity,
        bannerAdInfo: AdmobBannerAdInfo,
        testIdsInDebug: Boolean,
        onDone: () -> Unit,
        onFailed: (Exception) -> Unit
    ): AdmobAppBannerAdView {
        val safeId =
            if (MonetizationSharedConfig.isDebug && testIdsInDebug) AD_ID_TEST_BANNER else bannerAdInfo.id

        val bannerAdSize = when (bannerAdInfo) {
            is AdmobSimpleBannerAdInfo -> AdSize.BANNER
            is AdmobAdaptiveBannerAdInfo -> adaptiveBannerAdSize(activity)
            is AdmobInlineAdaptiveBannerAdInfo -> inlineAdaptiveBannerAdSize(
                activity,
                bannerAdInfo.heightDp.toInt()
            )

            is AdmobRectangularBannerAdInfo -> AdSize.MEDIUM_RECTANGLE
            else -> throw IllegalArgumentException("admob banner ad type not supported")
        }

        val isCollapsibleTop = when (bannerAdInfo) {
            is AdmobSimpleBannerAdInfo -> bannerAdInfo.isCollapsibleTop
            is AdmobAdaptiveBannerAdInfo -> bannerAdInfo.isCollapsibleTop
            else -> false
        }

        val isCollapsibleBottom = when (bannerAdInfo) {
            is AdmobSimpleBannerAdInfo -> bannerAdInfo.isCollapsibleBottom
            is AdmobAdaptiveBannerAdInfo -> bannerAdInfo.isCollapsibleBottom
            else -> false
        }

        val bannerAdView = AdView(activity).apply {
            populate(
                id = safeId,
                adSize = bannerAdSize,
                adTag = bannerAdInfo.tag,
                collapsibleTop = isCollapsibleTop,
                collapsibleBottom = isCollapsibleBottom,
                onDone = onDone,
                onFailed = onFailed
            )
        }
        return AdmobAppBannerAdView(bannerAdView)
    }

    override fun getShimmerHeight(
        activity: Activity,
        bannerAdInfo: AdmobBannerAdInfo
    ): Int = when (val adInfo = bannerAdInfo) {
        is AdmobSimpleBannerAdInfo -> 50
        is AdmobAdaptiveBannerAdInfo -> 116
        is AdmobInlineAdaptiveBannerAdInfo -> adInfo.heightDp.toInt()
        is AdmobRectangularBannerAdInfo -> 250
        else -> 60
    }

    private fun AdView.populate(
        id: String,
        adSize: AdSize,
        adTag: String,
        collapsibleTop: Boolean,
        collapsibleBottom: Boolean,
        onDone: () -> Unit,
        onFailed: (Exception) -> Unit,
    ) {
        val adRequest = if (collapsibleTop || collapsibleBottom) {
            val extras = Bundle()
            if (collapsibleTop) {
                extras.putString("collapsible", "top")
            } else {
                extras.putString("collapsible", "bottom")
            }
            BannerAdRequest.Builder(id, adSize)
                .setGoogleExtrasBundle(extras)
                .build()
        } else {
            BannerAdRequest.Builder(id, adSize)
                .build()
        }
        loadAd(adRequest, object : AdLoadCallback<BannerAd> {

            override fun onAdLoaded(ad: BannerAd) {
                MainScope().launch {
                    Log.d("AdsLib: ", "$adTag Ad was loaded.")
                    Log.d(TAG, "onAdLoaded: $adTag")
                    onDone()
                }
                ad.adEventCallback = object : BannerAdEventCallback {
                    override fun onAdPaid(value: AdValue) {
                        val extraMap: MutableMap<String, Any> = mutableMapOf()

                        AdmobNetworkApp.instance.revenueListener.onRevenue(
                            revenueModel = AdmobRevenue(
                                adValue = value,
                                extras = extraMap,
                                adUnitId = id,
                                adType = "BANNER"
                            )
                        )
                    }
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                MainScope().launch {
                    Log.d("AdsLib: ", "failed to load ${adError.message}")
                    Log.d(TAG, "onAdFailedToLoad: $adTag $adError")
                    onFailed(Exception(adError.message))
                }
            }
        })
    }

    companion object {

        private const val TAG = "BannerAdPopulateManagerTAG"
    }
}
