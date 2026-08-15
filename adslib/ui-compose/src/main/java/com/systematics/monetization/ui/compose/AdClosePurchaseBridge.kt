package com.systematics.monetization.ui.compose

import android.app.Activity

/**
 * Bridge that lets the host app react to the inline-ad "remove ads" cross button
 * without the monetization module knowing anything about the app's navigation or
 * billing. The app sets [onAdCloseClicked] (e.g. to open the paywall); the inline
 * ad component invokes it when the cross is tapped.
 */
object AdClosePurchaseBridge {
    var onAdCloseClicked: ((Activity) -> Unit)? = null
}
