package com.systematics.photocleaner.swipedelete.domain.swipedelete

import org.junit.Assert.assertEquals
import org.junit.Test

class SwipeDeleteRulesTest {
    @Test fun swipe_below_threshold_returns_to_rest() {
        assertEquals(SwipeOutcome.Rest, SwipeDecisionEngine.outcome(27f, 100f, 500f))
    }
    @Test fun right_and_left_swipes_keep_direction_invariant() {
        assertEquals(SwipeOutcome.Keep, SwipeDecisionEngine.outcome(29f, 100f, 0f))
        assertEquals(SwipeOutcome.Delete, SwipeDecisionEngine.outcome(-29f, 100f, 0f))
    }
    @Test fun fast_flick_commits_before_distance_threshold() {
        assertEquals(SwipeOutcome.Keep, SwipeDecisionEngine.outcome(4f, 100f, 1250f))
    }
    @Test fun level_and_streak_are_non_punitive() {
        assertEquals(1, GamificationRules.level(0))
        assertEquals(2, GamificationRules.level(1000))
        assertEquals(4, GamificationRules.streak(3, 9, 10))
        assertEquals(3, GamificationRules.streak(3, 10, 10))
        assertEquals(1, GamificationRules.streak(9, 7, 10))
    }
    @Test fun deletion_batches_never_exceed_platform_limit() {
        val batches = DeletionBatcher.batches((1..4501).toList())
        assertEquals(listOf(2000, 2000, 501), batches.map { it.size })
    }
}
