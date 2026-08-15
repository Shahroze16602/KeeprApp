package com.systematics.monetization.core.interfaces

import com.systematics.monetization.core.models.AdGroupResult

interface AdGroupLoadWListener {

    fun onAdLoaded(adType: String, getAdGroupResult: () -> AdGroupResult?)

    fun onAdLoadFailed(adType: String, exception: Exception)
}