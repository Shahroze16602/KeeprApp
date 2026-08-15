package com.systematics.monetization.core.models

import com.systematics.monetization.core.managers.ad.AdManager
import com.systematics.monetization.core.models.ad.local.AdInfo

data class AdManagerResult(
    val adInfo: AdInfo,
    val adManager: AdManager<*>
)