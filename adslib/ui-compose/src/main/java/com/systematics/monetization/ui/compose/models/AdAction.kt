package com.systematics.monetization.ui.compose.models

data class AdAction(
    val placementKey: String,
    val action: (Boolean) -> Unit,
)