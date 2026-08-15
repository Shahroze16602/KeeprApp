package com.systematics.monetization.core

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.systematics.monetization.core.analytics.AdsEvents
import com.systematics.monetization.core.analytics.AnalyticsEventsListener
import com.systematics.monetization.core.client.AdClient
import com.systematics.monetization.core.integration.IntegrationManager
import com.systematics.monetization.core.models.AdRequester
import com.systematics.monetization.core.models.AdsNetworkModel
import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.models.enums.AdsNetworkInitStrategy
import com.systematics.monetization.core.remote.AdsCoreRemoteConfigs
import com.systematics.monetization.core.utils.InternetController
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow


class MonetizationApp(
    internetController: InternetController,
    val context: Context,
    val adsCoreRemoteConfigs: AdsCoreRemoteConfigs
) {

    var isInitialized: MutableStateFlow<Boolean> = MutableStateFlow(false)
        private set

    var eventsListener = object : AnalyticsEventsListener {
        override fun onEvent(adsEvents: AdsEvents) {
            Log.d(TAG, "onEvent: $adsEvents")
        }
    }

    var integrationManager: IntegrationManager = IntegrationManager(
        context = context,
        allIntegrations = listOf()
    )
        private set

    val adClient = AdClient(context, internetController) { eventsListener.onEvent(it) }

    init {

        instance = this
    }

    suspend fun initializeMonetization(
        adsNetworkModels: List<AdsNetworkModel>,
        mapAdInfo: (AdInfo) -> AdInfo = { it },
    ) {
        integrationManager = IntegrationManager(
            context = context,
            mapAdInfo = mapAdInfo,
            allIntegrations = adsNetworkModels.map { it.adsNetwork }
        )
        initializeIntegrations(adsNetworkModels, AdsNetworkInitStrategy.EAGERLY)
        isInitialized.value = true
    }

    private suspend fun initializeIntegrations(
        adsNetworkModels: List<AdsNetworkModel>,
        currentInitStrategy: AdsNetworkInitStrategy
    ) {
        coroutineScope {
            val initTasksAsync: MutableList<Deferred<Boolean>> = mutableListOf()
            adsNetworkModels.forEach {
                initTasksAsync.add(
                    async {
                        if (it.initStrategy == currentInitStrategy) {
                            it.adsNetwork.initialize()
                            true
                        } else {
                            it.adsNetwork.defer()
                            false
                        }
                    }
                )
            }
            awaitAll(*initTasksAsync.toTypedArray())
        }
    }

    fun addRequester(adRequester: AdRequester) {
        adClient.addAdRequester(adRequester)
    }

    fun removeRequester(adRequester: AdRequester) {
        adClient.removeRequester(adRequester)
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        lateinit var instance: MonetizationApp
        private const val TAG = "MonetizationAppTAG"

    }
}