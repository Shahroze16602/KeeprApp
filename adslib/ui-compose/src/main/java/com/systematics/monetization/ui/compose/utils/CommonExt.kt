package com.systematics.monetization.ui.compose.utils

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.systematics.monetization.ui.compose.models.AdAction

fun MutableState<AdAction?>.showAd(
    placementKey: String,
    action: (Boolean) -> Unit,
) {
    if (this.value != null) {
        this.value = null
    }
    this.value = AdAction(
        placementKey = placementKey,
        action = {
            action(it)
            this.value = null
        }
    )
}

@Composable
inline fun <reified VM : ViewModel> activityViewModel(): VM {
    val activity = LocalActivity.current as? ComponentActivity
        ?: error("getActivityViewModel() must be called within a ComponentActivity context.")
    return viewModel(viewModelStoreOwner = activity)
}