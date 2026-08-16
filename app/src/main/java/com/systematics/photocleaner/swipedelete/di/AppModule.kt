package com.systematics.photocleaner.swipedelete.di

import com.systematics.billing.core.data.datasource.PremiumHandler
import com.systematics.billing.core.domain.usecase.BuyPremiumOfferUseCase
import com.systematics.billing.core.domain.usecase.ObservePremiumStateUseCase
import com.systematics.billing.core.domain.usecase.QueryPremiumOffersUseCase
import com.systematics.billing.core.domain.usecase.QueryPurchasedOfferUseCase
import com.systematics.billing.revenuecat.RevenueCatBillingPremiumHandler
import com.systematics.photocleaner.swipedelete.utils.update.AppUpdateHelper
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val appModule = module {
    factoryOf(::AppUpdateHelper)
    single<PremiumHandler> { RevenueCatBillingPremiumHandler(get()) }
    factory { QueryPremiumOffersUseCase() }
    factory { BuyPremiumOfferUseCase() }
    factory { ObservePremiumStateUseCase() }
    factory { QueryPurchasedOfferUseCase() }
}
