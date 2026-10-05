package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.settings.presentation.model.DisplayToggle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DynamicColorToggleRowMapperTest {
    @Nested
    inner class Key {
        @Test
        fun `keys the row DYNAMIC_COLOR and carries checked through`() {
            // ----- Arrange -----
            val checked = false

            // ----- Act -----
            val result = dynamicColorToggleRowFor(checked = checked)

            // ----- Assert -----
            result.key shouldBe DisplayToggle.DYNAMIC_COLOR
            result.checked shouldBe false
        }
    }
}
