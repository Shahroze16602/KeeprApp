package com.systematics.monetization.ui.placement.models

import androidx.annotation.Keep
import com.systematics.monetization.ui.placement.models.abs.AdGroupPlacement
import com.systematics.monetization.ui.placement.models.abs.PlacementModel
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class FullScreenPlacementModel(
    override val isEnabled: Boolean = true,
    override val tag: String = "",
    // adgroup
    override val adGroupType: String = "",
    override val consumableAdGroups: List<String> = listOf(),
    override val backupAdGroups: List<BackupModel> = listOf(),
    // preload
    val showLoadingDialog: Boolean = false,
    val loadingDialogTimeMillis: Long = 1000,
    // instant
    val isInstantAllowed: Boolean = true,
    val showInstantLoadingDialog: Boolean = true,
    val instantTimeoutSeconds: Long = 7,
    // dialog
    val dialogType: String = "default",
    // counter
    val counter: String = "",
    // timer
    val timer: String = "",
) : PlacementModel(), AdGroupPlacement