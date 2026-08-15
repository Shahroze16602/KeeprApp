package com.systematics.monetization.ui.placement.models.abs

import androidx.annotation.Keep
import com.systematics.monetization.ui.placement.models.BackupModel

@Keep
interface AdGroupPlacement {

    val adGroupType: String
    val consumableAdGroups: List<String>
    val backupAdGroups: List<BackupModel>
}