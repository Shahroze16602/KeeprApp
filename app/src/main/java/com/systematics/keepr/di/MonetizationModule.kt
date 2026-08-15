package com.systematics.keepr.di

import android.content.Context
import android.net.ConnectivityManager
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.utils.InternetController
import com.systematics.monetization.ui.remote.MonetizationRemote
import com.systematics.monetization.ui.remote.config.AdsUiRemoteConfigs
import com.systematics.monetization.ui.viewmodel.AdsViewModel
import com.systematics.keepr.utils.monetization.MonetizationHandler
import com.systematics.keepr.utils.monetization.config.backend.defaultAppAdsConfig
import com.systematics.keepr.utils.monetization.config.frontend.breakpoints.defaultAppBreakPointConfig
import com.systematics.keepr.utils.monetization.config.frontend.config.defaultAppAdsUiConfig
import com.systematics.keepr.utils.monetization.config.frontend.mappings.defaultAppAdsMappings
import com.systematics.keepr.utils.monetization.config.frontend.theme.defaultAppNativeUiConfig
import com.systematics.keepr.data.monetization.AdsCoreRemoteConfigsImpl
import com.systematics.keepr.data.monetization.AdsUiRemoteConfigsImpl
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val monetizationModule = module {
    single<AdsCoreRemoteConfigs> { AdsCoreRemoteConfigsImpl(get(), get()) }
    single<AdsUiRemoteConfigs> { AdsUiRemoteConfigsImpl(get()) }

    single {
        MonetizationRemote(
            defaultAppAdsConfig = defaultAppAdsConfig,
            defaultAppAdsUiConfig = defaultAppAdsUiConfig,
            defaultAppBreakPointConfig = defaultAppBreakPointConfig,
            defaultAppNativeUiConfig = defaultAppNativeUiConfig,
            defaultAppAdsMappingConfig = defaultAppAdsMappings,
            adsCoreRemoteConfigs = get(),
            uiRemoteConfig = get()
        )
    }

    single {
        val connectivityManager =
            get<Context>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        InternetController(connectivityManager)
    }

    singleOf(::MonetizationHandler)
    viewModelOf(::AdsViewModel)
}
