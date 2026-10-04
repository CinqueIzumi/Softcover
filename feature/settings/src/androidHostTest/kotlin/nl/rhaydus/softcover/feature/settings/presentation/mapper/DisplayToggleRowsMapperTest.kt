package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DisplayToggleRowsMapperTest {
    @Nested
    inner class PlatformFlags {
        @Test
        fun `includes the floating-bar and shelf-swipe rows when both flags are true`() {
            // ----- Act -----
            val result = displayToggleRowsFor(
                showFloatingBarToggle = true,
                showShelfSwipeToggle = true,
                useFloatingBarChecked = true,
                shelfSwipeEnabledChecked = true,
                readingStreakEnabledChecked = true,
            )

            // ----- Assert -----
            result.map { it.key } shouldBe listOf(
                DisplayToggle.FLOATING_BOTTOM_BAR,
                DisplayToggle.SHELF_SWIPE,
                DisplayToggle.READING_STREAK,
            )
        }

        @Test
        fun `drops the floating-bar row when showFloatingBarToggle is false`() {
            // ----- Act -----
            val result = displayToggleRowsFor(
                showFloatingBarToggle = false,
                showShelfSwipeToggle = true,
                useFloatingBarChecked = true,
                shelfSwipeEnabledChecked = true,
                readingStreakEnabledChecked = true,
            )

            // ----- Assert -----
            result.map { it.key } shouldBe listOf(DisplayToggle.SHELF_SWIPE, DisplayToggle.READING_STREAK)
        }

        @Test
        fun `drops the shelf-swipe row when showShelfSwipeToggle is false`() {
            // ----- Act -----
            val result = displayToggleRowsFor(
                showFloatingBarToggle = true,
                showShelfSwipeToggle = false,
                useFloatingBarChecked = true,
                shelfSwipeEnabledChecked = true,
                readingStreakEnabledChecked = true,
            )

            // ----- Assert -----
            result.map { it.key } shouldBe listOf(DisplayToggle.FLOATING_BOTTOM_BAR, DisplayToggle.READING_STREAK)
        }

        @Test
        fun `keeps only the reading-streak row when both platform flags are false`() {
            // ----- Act -----
            val result = displayToggleRowsFor(
                showFloatingBarToggle = false,
                showShelfSwipeToggle = false,
                useFloatingBarChecked = true,
                shelfSwipeEnabledChecked = true,
                readingStreakEnabledChecked = true,
            )

            // ----- Assert -----
            result.map { it.key } shouldBe listOf(DisplayToggle.READING_STREAK)
        }
    }

    @Nested
    inner class CheckedValues {
        @Test
        fun `carries each row's checked value from the matching state field`() {
            // ----- Act -----
            val result = displayToggleRowsFor(
                showFloatingBarToggle = true,
                showShelfSwipeToggle = true,
                useFloatingBarChecked = true,
                shelfSwipeEnabledChecked = false,
                readingStreakEnabledChecked = true,
            )

            // ----- Assert -----
            result.first { it.key == DisplayToggle.FLOATING_BOTTOM_BAR }.checked shouldBe true
            result.first { it.key == DisplayToggle.SHELF_SWIPE }.checked shouldBe false
            result.first { it.key == DisplayToggle.READING_STREAK }.checked shouldBe true
        }
    }
}
