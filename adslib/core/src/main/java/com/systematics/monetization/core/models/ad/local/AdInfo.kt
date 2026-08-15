package com.systematics.monetization.core.models.ad.local


import com.systematics.monetization.core.utils.EventsConstants.MATCHED_NORMAL

data class AdInfo(
    val adUnitId: String,
    val adType: String,
    val adTAG: String,
    val matchedTAG: String = MATCHED_NORMAL
)