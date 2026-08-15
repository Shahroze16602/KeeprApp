package com.systematics.monetization.core.integration.interfaces

import com.systematics.monetization.core.revenue.interfaces.RevenueListener

interface AdsNetworkApp {

    val adsLoadingPermitted: (adType: String) -> Boolean
    var revenueListener: RevenueListener
}