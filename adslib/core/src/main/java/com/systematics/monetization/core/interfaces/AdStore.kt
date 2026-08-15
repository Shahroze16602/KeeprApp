package com.systematics.monetization.core.interfaces

import android.util.Log

interface AdStore<T> {

    val adStore: MutableMap<String, T>

    fun preserveAd(
        placementKey: String,
        ad: T
    ) {
        Log.d(TAG, "${this.javaClass.simpleName} preserveAd: $placementKey")
        adStore[placementKey] = ad
    }

    fun isPreserved(placementKey: String): Boolean {
        return adStore[placementKey]?.let {
            Log.d(TAG, "${this.javaClass.simpleName} isPreserved: $placementKey preserved")
            true
        } ?: run {
            Log.d(TAG, "${this.javaClass.simpleName} isPreserved: $placementKey not preserved")
            false
        }
    }

    fun getPreservedAd(placementKey: String): T? {
        return adStore[placementKey]?.let {
            Log.d(TAG, "${this.javaClass.simpleName} getPreservedAd: $placementKey reused")
            it
        } ?: run {
            Log.d(TAG, "${this.javaClass.simpleName} getPreservedAd: $placementKey unavailable")
            null
        }
    }

    fun clear()

    companion object {

        private const val TAG = "AdStoreTAG"
    }

}