package nl.rhaydus.softcover.core.component.haptics

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReorderHapticsTest {
    @Nested
    inner class Moved {
        @Test
        fun `repeated reports of the same index tick once`() {
            // ----- Arrange -----
            val haptics = RecordingHaptics()
            val reorder = ReorderHaptics(haptics)

            // ----- Act -----
            reorder.moved(2)
            reorder.moved(2)
            reorder.moved(2)

            // ----- Assert -----
            haptics.calls shouldBe listOf("tickle")
        }

        @Test
        fun `a new index ticks again`() {
            // ----- Arrange -----
            val haptics = RecordingHaptics()
            val reorder = ReorderHaptics(haptics)

            // ----- Act -----
            reorder.moved(1)
            reorder.moved(2)

            // ----- Assert -----
            haptics.calls shouldBe listOf("tickle", "tickle")
        }

        @Test
        fun `moving to the lift index does not tick`() {
            // ----- Arrange -----
            val haptics = RecordingHaptics()
            val reorder = ReorderHaptics(haptics)
            reorder.lift(fromIndex = 3)
            haptics.calls.clear()

            // ----- Act -----
            reorder.moved(3)

            // ----- Assert -----
            haptics.calls shouldBe emptyList()
        }

        @Test
        fun `lifting without an index lets the first move tick`() {
            // ----- Arrange -----
            val haptics = RecordingHaptics()
            val reorder = ReorderHaptics(haptics)
            reorder.lift()
            haptics.calls.clear()

            // ----- Act -----
            reorder.moved(0)

            // ----- Assert -----
            haptics.calls shouldBe listOf("tickle")
        }
    }

    @Nested
    inner class Drop {
        @Test
        fun `drop plays a drop and tracking restarts on the next lift`() {
            // ----- Arrange -----
            val haptics = RecordingHaptics()
            val reorder = ReorderHaptics(haptics)
            reorder.lift(fromIndex = 1)
            reorder.moved(2)
            haptics.calls.clear()

            // ----- Act -----
            reorder.drop()
            reorder.lift()
            reorder.moved(2)

            // ----- Assert -----
            haptics.calls shouldBe listOf("drop", "lift", "tickle")
        }
    }
}
