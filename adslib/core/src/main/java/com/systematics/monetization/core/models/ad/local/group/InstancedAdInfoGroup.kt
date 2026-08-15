package com.systematics.monetization.core.models.ad.local.group

import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.core.models.AdRepeatInfo
import com.systematics.monetization.core.models.ad.local.group.abs.AdInfoGroup
import com.systematics.monetization.core.utils.mapValidated

data class InstancedAdInfoGroup(
    val instancedAdUnits: List<AdInfo>,
    override val adType: String,
    override val adTAG: String,
    override val singletonAd: Boolean,
    override val parkAfterImpression: Boolean,
    override val repeatInfo: AdRepeatInfo = AdRepeatInfo()
) : AdInfoGroup() {

    override fun validateAdInfoGroup(): InstancedAdInfoGroup {
        return this.copy(
            instancedAdUnits = instancedAdUnits.map { it.mapValidated() }
        )
    }
}