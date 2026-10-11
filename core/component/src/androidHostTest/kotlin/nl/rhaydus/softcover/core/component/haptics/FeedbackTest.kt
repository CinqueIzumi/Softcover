package nl.rhaydus.softcover.core.component.haptics

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FeedbackTest {
    @Nested
    inner class Play {
        @Test
        fun `routine feedback plays a selection tick`() {
            // ----- Arrange -----
            val haptics = RecordingHaptics()

            // ----- Act -----
            haptics.play(Feedback.Routine)

            // ----- Assert -----
            haptics.calls shouldBe listOf("select")
        }

        @Test
        fun `milestone feedback plays a commit`() {
            // ----- Arrange -----
            val haptics = RecordingHaptics()

            // ----- Act -----
            haptics.play(Feedback.Milestone)

            // ----- Assert -----
            haptics.calls shouldBe listOf("commit")
        }

        @Test
        fun `failure feedback plays a reject`() {
            // ----- Arrange -----
            val haptics = RecordingHaptics()

            // ----- Act -----
            haptics.play(Feedback.Failure)

            // ----- Assert -----
            haptics.calls shouldBe listOf("reject")
        }

        @Test
        fun `null feedback plays nothing`() {
            // ----- Arrange -----
            val haptics = RecordingHaptics()

            // ----- Act -----
            haptics.play(null)

            // ----- Assert -----
            haptics.calls shouldBe emptyList()
        }
    }
}
