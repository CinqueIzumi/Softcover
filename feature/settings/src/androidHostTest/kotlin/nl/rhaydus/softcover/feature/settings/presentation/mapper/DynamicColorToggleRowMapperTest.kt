package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DynamicColorToggleRowMapperTest {
    @Nested
    inner class Key {
        @Test
        fun `keys the row DYNAMIC_COLOR`() {
            // ----- Act -----
            val result = dynamicColorToggleRowFor(checked = true)

            // ----- Assert -----
            result.key shouldBe DisplayToggle.DYNAMIC_COLOR
        }
    }

    @Nested
    inner class CheckedValue {
        @Test
        fun `carries checked true through unchanged`() {
            // ----- Act -----
            val result = dynamicColorToggleRowFor(checked = true)

            // ----- Assert -----
            result.row.checked shouldBe true
        }

        @Test
        fun `carries checked false through unchanged`() {
            // ----- Act -----
            val result = dynamicColorToggleRowFor(checked = false)

            // ----- Assert -----
            result.row.checked shouldBe false
        }
    }
}
