package com.systematics.monetization.core.interfaces

import com.systematics.monetization.core.models.AdGroupResult

interface AdLoaderResponse : AdResponse {

    fun onAdLoaded(adGroupResult: AdGroupResult)
}