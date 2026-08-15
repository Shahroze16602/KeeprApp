package com.systematics.monetization.ui

import android.util.Log
import com.systematics.monetization.ui.remote.config.AdsUiRemoteConfigs

class AppAdsCounter(
    private val remoteConfigs: AdsUiRemoteConfigs
) {

    private val counterMap: MutableMap<String, Long> = mutableMapOf()

    fun isCounterReachedOnIncrement(
        counter: String,
        resetOnReached: Boolean = true
    ): Boolean {
        incrementCounter(counter)
        val isReached = isCounterReached(counter)
        if (isReached && resetOnReached) {
            resetCurrentCounterValue(counter)
        }
        return isReached
    }

    fun incrementCounter(
        counter: String,
        step: Long = 1
    ): Long {
        val previousCounter = geCounterCurrentValue(counter)
        val updateCounter = previousCounter + step
        counterMap[counter] = updateCounter
        Log.d(TAG, "incrementCounter: $counter ($previousCounter -> $updateCounter)")
        return updateCounter
    }

    fun decrementCounter(
        counter: String,
        step: Long = 1
    ): Long {
        val previousCounter = geCounterCurrentValue(counter)
        val updateCounter = previousCounter - step
        counterMap[counter] = updateCounter
        Log.d(TAG, "decrementCounter: $counter ($previousCounter -> $updateCounter)")
        return updateCounter
    }

    fun isCounterReached(counter: String): Boolean {
        val currentValue = geCounterCurrentValue(counter)
        val targetValue = getCounterTargetValue(counter)
        val isReached = currentValue >= targetValue
        Log.d(TAG, "isCounterReached: reached at ($currentValue, $targetValue) $isReached")
        return isReached
    }

    fun geCounterCurrentValue(counter: String): Long {
        val defaultCounterKey = "${counter}_default"
        return counterMap.getOrPut(counter) {
            val defaultValue = remoteConfigs.appAdCounterDefault(defaultCounterKey)
            Log.d(TAG, "getCounterValue: default counter for $counter $defaultValue")
            defaultValue
        }
    }

    fun getCounterTargetValue(counter: String): Long {
        return remoteConfigs.appAdCounter(counter)
    }

    fun clearCounterValue(placement: String) {
        counterMap.remove(placement)
        Log.d(TAG, "clearCounterValue: cleared for $placement")
    }

    fun resetCurrentCounterValue(placement: String) {
        counterMap[placement] = 0
        Log.d(TAG, "resetCounterValue: reset for $placement")
    }

    companion object {

        private const val TAG = "AppAdsCounterTAG"
    }
}