package nl.rhaydus.softcover.core.component.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SkeletonRevealPhaseTest {
    @Nested
    inner class WhileLoading {
        @Test
        fun `is Hidden one millisecond before the grace period ends`() {
            // ----- Arrange -----
            val elapsed = SKELETON_GRACE_MILLIS - 1

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = null,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Hidden
        }

        @Test
        fun `is Shimmer exactly when the grace period ends`() {
            // ----- Arrange -----
            val elapsed = SKELETON_GRACE_MILLIS

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = null,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Shimmer
        }

        @Test
        fun `stays Shimmer long after the grace period`() {
            // ----- Arrange -----
            val elapsed = SKELETON_GRACE_MILLIS + SKELETON_MINIMUM_VISIBLE_MILLIS * 100

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = null,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Shimmer
        }
    }

    @Nested
    inner class LoadingEndedInsideGracePeriod {
        @Test
        fun `is Content at the moment loading ended`() {
            // ----- Arrange -----
            val endedAt = SKELETON_GRACE_MILLIS - 50

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = endedAt,
                loadingEndedAtMillis = endedAt,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Content
        }
    }

    @Nested
    inner class LoadingEndedExactlyAtGracePeriod {
        private val endedAt = SKELETON_GRACE_MILLIS
        private val holdEnds = SKELETON_GRACE_MILLIS + SKELETON_MINIMUM_VISIBLE_MILLIS

        @Test
        fun `is Shimmer one millisecond before the minimum hold ends`() {
            // ----- Arrange -----
            val elapsed = holdEnds - 1

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = endedAt,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Shimmer
        }

        @Test
        fun `is Content when the minimum hold ends`() {
            // ----- Arrange -----
            val elapsed = holdEnds

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = endedAt,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Content
        }
    }

    @Nested
    inner class LoadingEndedBeforeMinimumHoldEnds {
        private val endedAt = SKELETON_GRACE_MILLIS + SKELETON_MINIMUM_VISIBLE_MILLIS - 50
        private val holdEnds = SKELETON_GRACE_MILLIS + SKELETON_MINIMUM_VISIBLE_MILLIS

        @Test
        fun `keeps the Shimmer until the minimum hold ends`() {
            // ----- Arrange -----
            val elapsed = holdEnds - 1

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = endedAt,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Shimmer
        }

        @Test
        fun `reveals Content when the minimum hold ends`() {
            // ----- Arrange -----
            val elapsed = holdEnds

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = endedAt,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Content
        }
    }

    @Nested
    inner class LoadingEndedAfterMinimumHoldEnds {
        private val endedAt = SKELETON_GRACE_MILLIS + SKELETON_MINIMUM_VISIBLE_MILLIS + 150

        @Test
        fun `is Shimmer one millisecond before loading ended`() {
            // ----- Arrange -----
            val elapsed = endedAt - 1

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = endedAt,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Shimmer
        }

        @Test
        fun `is Content when loading ended`() {
            // ----- Arrange -----
            val elapsed = endedAt

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = endedAt,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Content
        }
    }

    @Nested
    inner class SkippingGrace {
        @Test
        fun `is Shimmer immediately while loading`() {
            // ----- Arrange -----
            val elapsed = 0L

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = null,
                skipsGrace = true,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Shimmer
        }

        @Test
        fun `is Shimmer before the minimum hold ends when loading ended at once`() {
            // ----- Arrange -----
            val elapsed = SKELETON_MINIMUM_VISIBLE_MILLIS - 1

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = 0L,
                skipsGrace = true,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Shimmer
        }

        @Test
        fun `is Content when the minimum hold ends after an immediate finish`() {
            // ----- Arrange -----
            val elapsed = SKELETON_MINIMUM_VISIBLE_MILLIS

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = 0L,
                skipsGrace = true,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Content
        }

        @Test
        fun `is Shimmer one millisecond before a late finish`() {
            // ----- Arrange -----
            val elapsed = SKELETON_MINIMUM_VISIBLE_MILLIS + 99

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = SKELETON_MINIMUM_VISIBLE_MILLIS + 100,
                skipsGrace = true,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Shimmer
        }

        @Test
        fun `is Content when a late finish arrives`() {
            // ----- Arrange -----
            val elapsed = SKELETON_MINIMUM_VISIBLE_MILLIS + 100

            // ----- Act -----
            val phase = skeletonRevealPhase(
                elapsedMillis = elapsed,
                loadingEndedAtMillis = SKELETON_MINIMUM_VISIBLE_MILLIS + 100,
                skipsGrace = true,
            )

            // ----- Assert -----
            phase shouldBe SkeletonRevealPhase.Content
        }
    }
}
