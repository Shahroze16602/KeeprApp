package com.systematics.billing.core.di

import com.systematics.billing.core.data.datasource.PremiumHandler
import com.systematics.billing.core.data.repository.PremiumRepositoryImpl
import com.systematics.billing.core.domain.repositories.PremiumRepository


object BillingDIContainer {

    lateinit var premiumRepository: PremiumRepository

    fun init(
        premiumHandler: PremiumHandler,
    ){
        this.premiumRepository = PremiumRepositoryImpl(premiumHandler)
    }
}