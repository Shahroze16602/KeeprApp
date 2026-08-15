package com.systematics.monetization.admob.ui.utils

import android.app.Activity
import android.os.Build
import android.view.WindowMetrics
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import kotlin.math.roundToInt

val getFullWidth: (Activity) -> Int = { activity ->
    val displayMetrics = activity.resources.displayMetrics
    val adWidthPixels =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            displayMetrics.widthPixels
        }
    val density = displayMetrics.density
    (adWidthPixels / density).roundToInt()
}

fun adaptiveBannerAdSize(activity: Activity): AdSize {
    val adWidth = getFullWidth(activity)
    val adSize = AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, adWidth)
    return adSize
}

fun inlineAdaptiveBannerAdSize(activity: Activity, maxHeight: Int): AdSize {
    val adWidth = getFullWidth(activity)
    val adSize = AdSize.getInlineAdaptiveBannerAdSize(adWidth, maxHeight)
    return adSize
}