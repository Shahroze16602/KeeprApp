package com.systematics.monetization.core.utils

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "TimeoutTaskTAG"

fun timeoutTask(
    coroutineScope: CoroutineScope,
    maxTimeMillis: Long,
    task: suspend () -> Unit,
    onDone: () -> Unit,
    onTimeout: () -> Unit,
    onFailed: (Exception) -> Unit
) {
    coroutineScope.launch {
        Log.d(TAG, "timeoutTask: launching")
        try {
            withTimeout(maxTimeMillis) {
                Log.d(TAG, "timeoutTask: starting timeout task")
                task()
                onDone()
                Log.d(TAG, "timeoutTask: done timeout task")
            }
        }catch (ex: TimeoutCancellationException){
            onTimeout()
            Log.e(TAG, "timeoutTask: timeout ",ex)
        }
        catch (ex: Exception) {
            onFailed(ex)
            Log.e(TAG, "timeoutTask: failed ", ex)
        }
    }
}