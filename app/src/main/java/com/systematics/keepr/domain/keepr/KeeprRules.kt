package com.systematics.keepr.domain.keepr

import kotlin.math.abs

enum class SwipeOutcome { Keep, Delete, Rest }

object SwipeDecisionEngine {
    const val COMMIT_FRACTION = 0.28f
    const val COMMIT_VELOCITY_PX_PER_SECOND = 1250f
    fun outcome(displacementPx: Float, cardWidthPx: Float, velocityPxPerSecond: Float): SwipeOutcome {
        if (cardWidthPx <= 0f) return SwipeOutcome.Rest
        val commits = abs(displacementPx) >= cardWidthPx * COMMIT_FRACTION ||
            abs(velocityPxPerSecond) >= COMMIT_VELOCITY_PX_PER_SECOND
        if (!commits) return SwipeOutcome.Rest
        return if (displacementPx >= 0f) SwipeOutcome.Keep else SwipeOutcome.Delete
    }
}

object GamificationRules {
    const val XP_PER_DECISION = 100
    const val XP_PER_COMPLETION = 200
    const val XP_PER_LEVEL = 1000
    const val COMBO_WINDOW_MS = 2500L
    fun level(totalXp: Int) = totalXp.coerceAtLeast(0) / XP_PER_LEVEL + 1
    fun streak(previous: Int, lastEpochDay: Long?, todayEpochDay: Long): Int = when {
        lastEpochDay == todayEpochDay -> previous
        lastEpochDay == todayEpochDay - 1 -> previous + 1
        else -> 1
    }
}

object DeletionBatcher {
    const val PLATFORM_LIMIT = 2000
    fun <T> batches(items: List<T>): List<List<T>> = items.chunked(PLATFORM_LIMIT)
}
