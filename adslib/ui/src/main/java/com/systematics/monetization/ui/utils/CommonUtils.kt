package com.systematics.monetization.ui.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.LinearInterpolator

const val ADS_SHIMMER_TAG = 2103921309


fun View.startShimmerColor(baseColor: Int = android.graphics.Color.parseColor("#EAEAEA")) {
    // avoid starting multiple animators
    if (getTag(ADS_SHIMMER_TAG) != null) return

    val to = (baseColor and 0x00FFFFFF) or ((0.3f * 255).toInt() shl 24)

    val animator = ValueAnimator.ofObject(ArgbEvaluator(), baseColor, to).apply {
        duration = 700L
        repeatMode = ValueAnimator.REVERSE
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()

        addUpdateListener {
            this@startShimmerColor.setBackgroundColor(it.animatedValue as Int)
        }
        start()
    }

    setTag(ADS_SHIMMER_TAG, animator)
}

fun View.stopShimmerColor() {
    val animator = getTag(ADS_SHIMMER_TAG) as? ValueAnimator ?: return
    animator.cancel()
    setBackgroundColor(android.graphics.Color.TRANSPARENT)
    setTag(ADS_SHIMMER_TAG, null)
}