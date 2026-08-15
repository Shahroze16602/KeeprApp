package com.systematics.monetization.admob.integration

import android.content.Context
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import com.systematics.monetization.admob.AdmobNetworkApp
import com.systematics.monetization.admob.managers.ad.AppOpenAdManager
import com.systematics.monetization.admob.managers.ad.AppOpenPreloadAdManager
import com.systematics.monetization.admob.managers.ad.InterstitialAdManager
import com.systematics.monetization.admob.managers.ad.InterstitialPreloadAdManager
import com.systematics.monetization.admob.managers.ad.NativeAdManager
import com.systematics.monetization.admob.managers.ad.RewardedAdManager
import com.systematics.monetization.admob.managers.ad.RewardedInterstitialAdManager
import com.systematics.monetization.admob.managers.ad.RewardedPreloadAdManager
import com.systematics.monetization.admob.models.natives.AdmobAppNativeAd
import com.systematics.monetization.admob.ui.AdmobBannerPopulateManager
import com.systematics.monetization.admob.ui.store.defaultAdmobNativeRegistryModel
import com.systematics.monetization.admob.utils.AdmobAdType
import com.systematics.monetization.admob.utils.getDebugAdUnitForAdType
import com.systematics.monetization.core.BuildConfig
import com.systematics.monetization.core.integration.interfaces.AdsNetworkIntegration
import com.systematics.monetization.core.integration.natives.NativeRegistryModel
import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.managers.populate.BannerPopulateManager
import com.systematics.monetization.core.models.AdsNetworkState
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.revenue.interfaces.RevenueListener
import com.systematics.monetization.core.utils.ALog
import com.systematics.monetization.core.utils.APP_ID_TEST
import com.systematics.monetization.core.utils.MonetizationBuildConfig
import com.systematics.monetization.core.utils.MonetizationSharedConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class AdmobIntegration(
    private val appId: String,
    private val context: Context,
    private val onPassConsent: () -> Unit = {},
    admobNativeRegistry: NativeRegistryModel<AdmobAppNativeAd> = defaultAdmobNativeRegistryModel,
    adsLoadingPermitted: (adType: String) -> Boolean = { true }
) : AdsNetworkIntegration {

    override val adsNetworkApp = AdmobNetworkApp(adsLoadingPermitted)

    override var adsNetworkState: AdsNetworkState = AdsNetworkState.Created

    override val adTypes: List<String> = AdmobAdType.all

    override val bannerPopulateManager: BannerPopulateManager<*> = AdmobBannerPopulateManager()

    override val nativeRegistry: NativeRegistryModel<AdmobAppNativeAd> = admobNativeRegistry
    private val initializationMutex = Mutex()

    init {

        if (!BuildConfig.DEBUG && MonetizationSharedConfig.buildConfig == MonetizationBuildConfig.RELEASE && appId == APP_ID_TEST) {
            throw IllegalArgumentException("Test app id cannot be used in release build release config")
        }
    }

    override suspend fun initialize() = withContext(Dispatchers.IO) {
        initializationMutex.withLock {
            if (adsNetworkState is AdsNetworkState.Initialized) return@withContext true
            passConsent(true)
            ALog.d(TAG, "admob initializing...")
            suspendCancellableCoroutine { continuation ->
                val config = InitializationConfig.Builder(appId).build()
                MobileAds.initialize(context, config) {
                    adsNetworkState = AdsNetworkState.Initialized
                    ALog.d(TAG, "admob initialized")
                    continuation.resume(true)
                }
            }
        }
    }

    override suspend fun defer() {
        if (adsNetworkState is AdsNetworkState.Created) {
            ALog.d(TAG, "admob deferred")
        }
    }

    override suspend fun createAdManager(adInfo: AdInfo): AdManager<*> {
        if (adsNetworkState == AdsNetworkState.Created) {
            Log.d(TAG, "createAdManager: deferred initializing admob network")
            initialize()
        }
        if (!isReady()) {
            throw IllegalStateException("Admob Ads network not ready")
        }
        Log.d(TAG, "createAdManager: creating admob manager for ${adInfo.adType}")
        return when (adInfo.adType) {
            AdmobAdType.INTERSTITIAL -> InterstitialAdManager(context, adInfo)
            AdmobAdType.INTERSTITIAL_PRE_LOAD -> InterstitialPreloadAdManager(context, adInfo)
            AdmobAdType.REWARDED -> RewardedAdManager(context, adInfo)
            AdmobAdType.REWARDED_PRE_LOAD -> RewardedPreloadAdManager(context, adInfo)
            AdmobAdType.REWARDED_INTERSTITIAL -> RewardedInterstitialAdManager(context, adInfo)
            AdmobAdType.NATIVE -> NativeAdManager(context, adInfo)
            AdmobAdType.APP_OPEN -> AppOpenAdManager(context, adInfo)
            AdmobAdType.APP_OPEN_PRE_LOAD -> AppOpenPreloadAdManager(context, adInfo)
            else -> throw IllegalArgumentException("Unknown ad type: ${adInfo.adType}")
        }
    }

    override fun mapTestIds(adType: String, adUnitId: String): String {
        return getDebugAdUnitForAdType(adType)
    }

    override fun attachRevenueListener(revenueListener: RevenueListener) {
        adsNetworkApp.revenueListener = revenueListener
    }

    override fun passConsent(consentGranted: Boolean) {
        ALog.d(TAG, "admob passing consent")
        if (consentGranted) onPassConsent()
    }

    companion object {

        private const val TAG = "Admob Integration"
    }
}