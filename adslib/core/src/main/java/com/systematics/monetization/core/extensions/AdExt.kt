package com.systematics.monetization.core.extensions

import android.util.Log
import com.systematics.monetization.core.models.AdRepeatInfo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

private const val TAG = "AdExtTAG"

suspend fun AdRepeatInfo.afterDelayedRepeat(repeatTask: suspend () -> Unit) {
    Log.d(TAG, "afterDelayedRepeat: delaying for $currentRepeatMillis")
    delay(currentRepeatMillis)
    coroutineScope { launch { repeatTask() } }
    currentRepeatMillis = ((currentRepeatMillis * multiplier).toLong())
    Log.d(TAG, "setOnRepeat: new $currentRepeatMillis")
    currentRepeatMillis = min(currentRepeatMillis, finalRepeatMillis)
    Log.d(TAG, "setOnRepeat: limited to $currentRepeatMillis")
}