package com.systematics.monetization.ui.utils

import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.ui.AppAdsCounter
import com.systematics.monetization.ui.AppAdsTimer
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.ad.FullScreenAdShowManager
import com.systematics.monetization.ui.ad.load.NativeLoadManager
import com.systematics.monetization.ui.di.MonetizationDIContainer
import com.systematics.monetization.ui.placement.AppAdsPlacements
import com.systematics.monetization.ui.remote.MonetizationRemote
import com.systematics.monetization.ui.remote.config.AdsUiRemoteConfigs

inline fun <reified T> MonetizationDIContainer.get(): T =
    when (T::class) {
        MonetizationApp::class -> monetizationApp
        MonetizationRemote::class -> monetizationRemote
        NativeLoadManager::class -> nativeLoadManager
        AdsCoreRemoteConfigs::class -> adsCoreRemoteConfigs
        MonetizationInstall::class -> monetizationInstall
        AdsUiRemoteConfigs::class -> adsUiRemoteConfigs
        AppAdsCounter::class -> appAdsCounter
        AppAdsTimer::class -> appAdsTimer
        AppAdsPlacements::class -> appAdsPlacements
        FullScreenAdShowManager::class -> fullScreenAdShowManager
        else -> error("No dependency found for ${T::class}")
    } as T

inline fun <reified T> monetizationInject(): Lazy<T> = lazy(LazyThreadSafetyMode.NONE) {
    MonetizationDIContainer.get()
}

inline fun <reified T> monetizationGet(): T = MonetizationDIContainer.get()