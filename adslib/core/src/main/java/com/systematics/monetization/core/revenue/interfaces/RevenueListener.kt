package com.systematics.monetization.core.revenue.interfaces

import com.systematics.monetization.core.revenue.models.RevenueModel

interface RevenueListener {

    fun <T : RevenueModel> onRevenue(revenueModel: T)
}