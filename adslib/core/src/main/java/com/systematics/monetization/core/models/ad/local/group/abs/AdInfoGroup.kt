package com.systematics.monetization.core.models.ad.local.group.abs

import com.systematics.monetization.core.models.AdRepeatInfo

abstract class AdInfoGroup {
    abstract val adType: String
    abstract val adTAG: String
    abstract val singletonAd: Boolean
    abstract val parkAfterImpression: Boolean
    abstract val repeatInfo: AdRepeatInfo

    abstract fun validateAdInfoGroup(): AdInfoGroup
}