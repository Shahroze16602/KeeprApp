package com.systematics.monetization.ui.ad.ui

import com.systematics.monetization.ui.models.ConditionalModel

data class AppAdsMappingConfig(
    val appAdsConditionalMappings: Map<String, List<ConditionalModel>>
)