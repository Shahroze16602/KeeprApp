package com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.mappings.local

import com.systematics.monetization.ui.models.ConditionalModel
import com.systematics.photocleaner.swipedelete.utils.monetization.config.frontend.mappings.AdConditions

val appAdsConditionalMappingConfig = mapOf(
    AdConditions.VPN_CONNECTED to emptyList<ConditionalModel>()
)
