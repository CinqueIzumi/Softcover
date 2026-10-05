package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DisplayToggleRowsMapperTest {
    @Nested
    inner class PlatformFlags {
        @Test
        fun `drops the floating-bar row when showFloatingBarToggle is false`() {
            // ----- Arrange -----
            val showFloatingBarToggle = false
            val showShelfSwipeToggle = true
            val useFloatingBarChecked = true
            val shelfSwipeEnabledChecked = true
            val readingStreakEnabledChecked = true

            // ----- Act -----
            val result = displayToggleRowsFor(
                showFloatingBarToggle = showFloatingBarToggle,
                showShelfSwipeToggle = showShelfSwipeToggle,
                useFloatingBarChecked = useFloatingBarChecked,
                shelfSwipeEnabledChecked = shelfSwipeEnabledChecked,
                readingStreakEnabledChecked = readingStreakEnabledChecked,
            )

            // ----- Assert -----
            result.map { it.key } shouldBe listOf(DisplayToggle.SHELF_SWIPE, DisplayToggle.READING_STREAK)
        }

        @Test
        fun `drops the shelf-swipe row when showShelfSwipeToggle is false`() {
            // ----- Arrange -----
            val showFloatingBarToggle = true
            val showShelfSwipeToggle = false
            val useFloatingBarChecked = true
            val shelfSwipeEnabledChecked = true
            val readingStreakEnabledChecked = true

            // ----- Act -----
            val result = displayToggleRowsFor(
                showFloatingBarToggle = showFloatingBarToggle,
                showShelfSwipeToggle = showShelfSwipeToggle,
                useFloatingBarChecked = useFloatingBarChecked,
                shelfSwipeEnabledChecked = shelfSwipeEnabledChecked,
                readingStreakEnabledChecked = readingStreakEnabledChecked,
            )

            // ----- Assert -----
            result.map { it.key } shouldBe listOf(DisplayToggle.FLOATING_BOTTOM_BAR, DisplayToggle.READING_STREAK)
        }

        @Test
        fun `keeps only the reading-streak row when both platform flags are false`() {
            // ----- Arrange -----
            val showFloatingBarToggle = false
            val showShelfSwipeToggle = false
            val useFloatingBarChecked = true
            val shelfSwipeEnabledChecked = true
            val readingStreakEnabledChecked = true

            // ----- Act -----
            val result = displayToggleRowsFor(
                showFloatingBarToggle = showFloatingBarToggle,
                showShelfSwipeToggle = showShelfSwipeToggle,
                useFloatingBarChecked = useFloatingBarChecked,
                shelfSwipeEnabledChecked = shelfSwipeEnabledChecked,
                readingStreakEnabledChecked = readingStreakEnabledChecked,
            )

            // ----- Assert -----
            result.map { it.key } shouldBe listOf(DisplayToggle.READING_STREAK)
        }
    }

    @Nested
    inner class CheckedValues {
        @Test
        fun `lists every row and carries each checked value from the matching state field`() {
            // ----- Arrange -----
            val showFloatingBarToggle = true
            val showShelfSwipeToggle = true
            val useFloatingBarChecked = true
            val shelfSwipeEnabledChecked = false
            val readingStreakEnabledChecked = true

            // ----- Act -----
            val result = displayToggleRowsFor(
                showFloatingBarToggle = showFloatingBarToggle,
                showShelfSwipeToggle = showShelfSwipeToggle,
                useFloatingBarChecked = useFloatingBarChecked,
                shelfSwipeEnabledChecked = shelfSwipeEnabledChecked,
                readingStreakEnabledChecked = readingStreakEnabledChecked,
            )

            // ----- Assert -----
            result.map { it.key } shouldBe listOf(
                DisplayToggle.FLOATING_BOTTOM_BAR,
                DisplayToggle.SHELF_SWIPE,
                DisplayToggle.READING_STREAK,
            )
            result.first { it.key == DisplayToggle.FLOATING_BOTTOM_BAR }.checked shouldBe true
            result.first { it.key == DisplayToggle.SHELF_SWIPE }.checked shouldBe false
            result.first { it.key == DisplayToggle.READING_STREAK }.checked shouldBe true
        }
    }
}
