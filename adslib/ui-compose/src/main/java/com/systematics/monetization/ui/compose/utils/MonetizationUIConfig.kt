package com.systematics.monetization.ui.compose.utils

import androidx.compose.runtime.Composable
import com.systematics.monetization.ui.ad.store.AdsStoreManager

object MonetizationUIConfig {

    var adsStoreManagerProvider: (@Composable () -> AdsStoreManager)? = null
}