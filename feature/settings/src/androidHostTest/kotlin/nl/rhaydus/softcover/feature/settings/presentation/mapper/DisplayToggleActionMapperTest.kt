package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDynamicColorToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnFloatingBarToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnReadingStreakToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnShelfSwipeToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DisplayToggleActionMapperTest {
    @Nested
    inner class KeyToAction {
        @Test
        fun `maps FLOATING_BOTTOM_BAR to OnFloatingBarToggledAction carrying newValue`() {
            // ----- Act -----
            val result = displayToggleActionFor(
                key = DisplayToggle.FLOATING_BOTTOM_BAR,
                newValue = true,
            )

            // ----- Assert -----
            val action = result.shouldBeInstanceOf<OnFloatingBarToggledAction>()
            action.newValue shouldBe true
        }

        @Test
        fun `maps SHELF_SWIPE to OnShelfSwipeToggledAction carrying newValue`() {
            // ----- Act -----
            val result = displayToggleActionFor(
                key = DisplayToggle.SHELF_SWIPE,
                newValue = false,
            )

            // ----- Assert -----
            val action = result.shouldBeInstanceOf<OnShelfSwipeToggledAction>()
            action.newValue shouldBe false
        }

        @Test
        fun `maps READING_STREAK to OnReadingStreakToggledAction carrying newValue`() {
            // ----- Act -----
            val result = displayToggleActionFor(
                key = DisplayToggle.READING_STREAK,
                newValue = true,
            )

            // ----- Assert -----
            val action = result.shouldBeInstanceOf<OnReadingStreakToggledAction>()
            action.newValue shouldBe true
        }

        @Test
        fun `maps DYNAMIC_COLOR to OnDynamicColorToggledAction carrying newValue`() {
            // ----- Act -----
            val result = displayToggleActionFor(
                key = DisplayToggle.DYNAMIC_COLOR,
                newValue = false,
            )

            // ----- Assert -----
            val action = result.shouldBeInstanceOf<OnDynamicColorToggledAction>()
            action.newValue shouldBe false
        }
    }
}
