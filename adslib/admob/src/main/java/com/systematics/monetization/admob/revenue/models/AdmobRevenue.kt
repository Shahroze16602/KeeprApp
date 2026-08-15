package com.systematics.monetization.admob.revenue.models

import com.google.android.libraries.ads.mobile.sdk.common.AdValue
import com.systematics.monetization.core.revenue.models.RevenueModel

data class AdmobRevenue(
    val adValue: AdValue,
    val extras: Map<String, Any> = emptyMap(),
    val country: String? = null,
    val adUnitId: String? = null,
    val adType: String? = null,
    val placement: String? = null
) : RevenueModel()