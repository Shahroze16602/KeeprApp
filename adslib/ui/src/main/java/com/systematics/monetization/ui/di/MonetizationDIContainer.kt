package com.systematics.monetization.ui.di

import com.systematics.monetization.core.MonetizationApp
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.ui.AppAdsCounter
import com.systematics.monetization.ui.AppAdsTimer
import com.systematics.monetization.ui.MonetizationInstall
import com.systematics.monetization.ui.ad.FullScreenAdShowManager
import com.systematics.monetization.ui.ad.load.NativeLoadManager
import com.systematics.monetization.ui.placement.AppAdsPlacements
import com.systematics.monetization.ui.remote.MonetizationRemote
import com.systematics.monetization.ui.remote.config.AdsUiRemoteConfigs
import com.systematics.monetization.core.utils.InternetController

object MonetizationDIContainer {

    @PublishedApi
    internal lateinit var monetizationApp: MonetizationApp


    @PublishedApi
    internal lateinit var fullScreenAdShowManager: FullScreenAdShowManager

    @PublishedApi
    internal lateinit var monetizationRemote: MonetizationRemote
    @PublishedApi
    internal lateinit var nativeLoadManager: NativeLoadManager
    @PublishedApi
    internal lateinit var adsCoreRemoteConfigs: AdsCoreRemoteConfigs
    @PublishedApi
    internal lateinit var monetizationInstall: MonetizationInstall

    @PublishedApi
    internal lateinit var adsUiRemoteConfigs: AdsUiRemoteConfigs
    @PublishedApi
    internal lateinit var appAdsCounter: AppAdsCounter
    @PublishedApi
    internal lateinit var appAdsTimer: AppAdsTimer

    @PublishedApi
    internal lateinit var appAdsPlacements: AppAdsPlacements

    fun init(
        monetizationApp: MonetizationApp,
        internetController: InternetController,
        monetizationRemote: MonetizationRemote,
        adsCoreRemoteConfigs: AdsCoreRemoteConfigs,
        adsUiRemoteConfigs: AdsUiRemoteConfigs
    ) {
        this.monetizationApp = monetizationApp
        this.monetizationRemote = monetizationRemote
        this.adsCoreRemoteConfigs = adsCoreRemoteConfigs
        this.adsUiRemoteConfigs = adsUiRemoteConfigs


        this.monetizationInstall = MonetizationInstall(
            adsCoreRemoteConfigs = adsCoreRemoteConfigs,
            monetizationRemote = monetizationRemote
        )
        this.nativeLoadManager = NativeLoadManager(
            monetizationApp = monetizationApp,
            monetizationInstall = monetizationInstall,
            adsCoreRemoteConfigs = adsCoreRemoteConfigs
        )
        this.appAdsCounter = AppAdsCounter(adsUiRemoteConfigs)
        this.appAdsTimer = AppAdsTimer(adsUiRemoteConfigs)
        this.appAdsPlacements = AppAdsPlacements(
            monetizationRemote = monetizationRemote,
            adsCoreRemoteConfigs = adsCoreRemoteConfigs
        )
        this.fullScreenAdShowManager = FullScreenAdShowManager(
            monetizationApp = monetizationApp,
            monetizationInstall = monetizationInstall,
            internetController = internetController,
            adsCoreRemoteConfigs = adsCoreRemoteConfigs
        )
    }
}