package com.systematics.monetization.ui.utils

import com.systematics.monetization.core.models.ad.local.AdInfo
import com.systematics.monetization.ui.models.ConditionalModel

fun applyMapping(
    original: AdInfo,
    rule: ConditionalModel
): AdInfo {
    val input = rule.input
    val output = rule.output

    // Collect non-empty input fields
    val checks = listOfNotNull(
        input.adUnitId.takeIf { it.isNotEmpty() }?.let { it == original.adUnitId },
        input.adType.takeIf { it.isNotEmpty() }?.let { it == original.adType },
        input.adTAG.takeIf { it.isNotEmpty() }?.let { it == original.adTAG },
        input.matchedTAG.takeIf { it.isNotEmpty() }?.let { it == original.matchedTAG }
    )

    // If there are NO conditions, don't match
    if (checks.isEmpty()) return original

    // All conditions must be true to match
    val matches = checks.all { it }
    if (!matches) return original

    // If eligible, apply NON-empty fields from output
    return AdInfo(
        adUnitId = output.adUnitId.ifEmpty { original.adUnitId },
        adType = output.adType.ifEmpty { original.adType },
        adTAG = output.adTAG.ifEmpty { original.adTAG },
        matchedTAG = output.matchedTAG.ifEmpty { original.matchedTAG }
    )
}