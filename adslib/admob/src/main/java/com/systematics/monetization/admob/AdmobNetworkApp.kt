package com.systematics.monetization.admob

import android.util.Log
import com.systematics.monetization.core.integration.interfaces.AdsNetworkApp
import com.systematics.monetization.core.revenue.interfaces.RevenueListener
import com.systematics.monetization.core.revenue.models.RevenueModel

class AdmobNetworkApp(
    override val adsLoadingPermitted: (adType: String) -> Boolean
) : AdsNetworkApp {

    init {

        instance = this
    }

    override var revenueListener: RevenueListener = object : RevenueListener {
        override fun <T : RevenueModel> onRevenue(revenueModel: T) {
            Log.d(TAG, "onRevenue: $revenueModel")
        }
    }

    companion object {

        lateinit var instance: AdmobNetworkApp
        private const val TAG = "AdsLib: Admob: AdmobNetworkAppTAG"
    }
}