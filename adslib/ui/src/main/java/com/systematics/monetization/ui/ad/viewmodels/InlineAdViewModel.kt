package com.systematics.monetization.ui.ad.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel

class InlineAdViewModel : ViewModel() {

    private val inlineAdStore: MutableMap<String, Any> = mutableMapOf()

    init {

        Log.d(TAG, ": initialized $this")
    }

    fun isPreservedFor(placement: String): Boolean = inlineAdStore.contains(placement)

    fun preserveNow(placement: String, item: Any) {
        Log.d(TAG, "preserveNow: preserving $item for $placement")
        inlineAdStore[placement] = item
    }

    fun getPreserved(placement: String): Any? {
        val preservedAd = inlineAdStore[placement]
        Log.d(TAG, "getPreserved: $placement ${preservedAd != null}")
        return preservedAd
    }

    fun clearPreserve(placement: String) {
        Log.d(TAG, "clearPreserve: $placement")
        inlineAdStore.remove(placement)
    }

    override fun onCleared() {
        super.onCleared()
        inlineAdStore.clear()
    }

    companion object {

        private const val TAG = "InlineAdViewModelTAG"
    }
}