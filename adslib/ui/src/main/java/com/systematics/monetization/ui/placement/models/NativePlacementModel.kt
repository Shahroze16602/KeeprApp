package com.systematics.monetization.ui.placement.models

import androidx.annotation.Keep
import com.systematics.monetization.ui.placement.RefreshMode
import com.systematics.monetization.ui.placement.models.abs.AdGroupPlacement
import com.systematics.monetization.ui.placement.models.abs.InlinePlacementModel
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class NativePlacementModel(
    override val isEnabled: Boolean = true,
    override val tag: String = "",
    // adgroup
    override val adGroupType: String = "",
    override val consumableAdGroups: List<String> = listOf(),
    override val backupAdGroups: List<BackupModel> = listOf(),

    val refreshMode: RefreshMode = RefreshMode.None,
    val layout: String = "",
    val isInstantAllowed: Boolean = true,
    val preserved: Boolean = true,
) : InlinePlacementModel(), AdGroupPlacement