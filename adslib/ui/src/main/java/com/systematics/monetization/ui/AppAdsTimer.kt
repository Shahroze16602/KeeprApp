package com.systematics.monetization.ui

import android.util.Log
import com.systematics.monetization.ui.remote.config.AdsUiRemoteConfigs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class AppAdsTimer(
    private val remoteConfigs: AdsUiRemoteConfigs,
) {

    private val timerTasks: MutableMap<String, Job> = mutableMapOf()
    private val timerMap: ConcurrentMap<String, Long> = ConcurrentHashMap()
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun checkOnCall(placement: String): Boolean = isTimerReached(placement)

    fun startTimerForPlacement(
        placement: String,
        isReached: Boolean = false,
        isUtilized: Boolean = false,
    ) {
        Log.d(TAG, "startTimerForPlacement: timer for $placement")
        val timerValue = remoteConfigs.appAdTimer(placement)
        if (isReached) {
            timerMap[placement] = timerValue
            Log.d(TAG, "startTimerForPlacement: $placement reached at $timerValue")
        } else {
            val startingValue = if (isUtilized) {
                resetTimerValue(placement)
                timerMap[placement] ?: 0
            } else {
                getTimerDefault(placement)
            }
            val totalTimer = (timerValue - startingValue).toInt()
            val timerJob = coroutineScope.launch {
                Log.d(
                    TAG,
                    "startTimerForPlacement: timer for $placement of $timerValue starting from $startingValue timed for $totalTimer"
                )
                repeat(totalTimer) {
                    val currentCounter = getTimerValue(placement)
                    delay(1000)
                    val updatedCounter = currentCounter + 1
                    timerMap[placement] = updatedCounter
                    Log.d(
                        TAG,
                        "startTimerForPlacement: timer $currentCounter to $updatedCounter"
                    )
                }
            }
            timerTasks[placement] = timerJob
        }
    }

    fun isTimerReached(placement: String): Boolean {
        val currentValue = getTimerValue(placement)
        val timerValue = remoteConfigs.appAdTimer(placement)
        val isReached = currentValue >= timerValue

        Log.d(TAG, "isTimerReached: reached at ($currentValue, $timerValue) $isReached")
        return isReached
    }

    private fun getTimerValue(placement: String): Long {
        return timerMap.getOrPut(placement) {
            val defaultValue = getTimerDefault(placement)
            Log.d(TAG, "getTimerValue: default start timer for $placement $defaultValue")
            defaultValue
        }
    }

    private fun getTimerDefault(placement: String): Long {
        val defaultCounterKey = "${placement}_default"
        val defaultValue = remoteConfigs.appAdTimerDefault(defaultCounterKey)
        return defaultValue
    }

    private fun resetTimerValue(placement: String) {
        timerMap[placement] = 0
        Log.d(TAG, "clearTimerValue: cleared for $placement")
        timerTasks[placement]?.let {
            if (it.isActive && !it.isCompleted) {
                it.cancel()
            }
            timerTasks.remove(placement)
        }
    }

    companion object {

        private const val TAG = "AppAdsTimerTAG"
    }
}