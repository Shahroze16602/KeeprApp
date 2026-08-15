package com.systematics.monetization.core.models

import com.systematics.monetization.core.interfaces.AdResponse
import com.systematics.monetization.core.utils.emptyAdLoaderResponse

abstract class AdRequester {
    abstract val adGroupType: String
    abstract val adTag: String
    abstract val adResponse: AdResponse
}

data class AdRequesterWaited(
    override val adGroupType: String,
    override val adTag: String,
    override var adResponse: AdResponse = emptyAdLoaderResponse
) : AdRequester()

data class AdRequesterLoaded(
    override val adGroupType: String,
    override val adTag: String,
    override val adResponse: AdResponse
) : AdRequester()