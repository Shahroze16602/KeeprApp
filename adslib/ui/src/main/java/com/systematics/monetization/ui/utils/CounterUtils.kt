package com.systematics.monetization.ui.utils

import android.util.Log
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.delay

private const val TAG = "CounterUtilsTAG"


suspend fun counterOnLifecycle(
    lifecycle: Lifecycle,
    maxCounter: Long = 5,
    onTimeout: () -> Unit,
) {
    var currentCounterValue = 0
    while (currentCounterValue < maxCounter) {
        delay(1000)
        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            currentCounterValue++
            Log.d(TAG, "counterOnLifecycle: counter reached to $currentCounterValue")
        } else {
            Log.d(TAG, "counterOnLifecycle: no counter update for not resumed")
        }
    }
    onTimeout()
}