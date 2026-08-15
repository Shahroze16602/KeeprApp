package com.systematics.monetization.core.models.ad.local.group

import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.models.AdRepeatInfo
import com.systematics.monetization.core.models.ad.local.group.abs.AdInfoGroup
import com.systematics.monetization.core.utils.mapValidated

data class RotatedFallBackAdInfoGroup(
    val rotatedAdUnits: List<AdInfo>,
    val fallbackAdUnits: List<AdInfo>,
    override val adType: String,
    override val adTAG: String,
    override val singletonAd: Boolean,
    override val parkAfterImpression: Boolean,
    override val repeatInfo: AdRepeatInfo = AdRepeatInfo()
) : AdInfoGroup() {

    override fun validateAdInfoGroup(): RotatedFallBackAdInfoGroup {
        return this.copy(
            rotatedAdUnits = rotatedAdUnits.map { it.mapValidated() },
            fallbackAdUnits = fallbackAdUnits.map { it.mapValidated() },
        )
    }
}