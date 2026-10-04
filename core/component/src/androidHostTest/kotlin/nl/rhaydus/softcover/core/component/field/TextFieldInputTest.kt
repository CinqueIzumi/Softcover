package nl.rhaydus.softcover.core.component.field

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TextFieldInputTest {
    private val scheme = lightColorScheme()

    @Nested
    inner class PlaceholderInk {
        @Test
        fun `a disabled placeholder is dimmer than an enabled one`() {
            // ----- Act -----
            val enabled = placeholderInk(
                scheme,
                enabled = true,
            )
            val disabled = placeholderInk(
                scheme,
                enabled = false,
            )

            // ----- Assert -----
            (disabled.alpha < enabled.alpha) shouldBe true
        }

        @Test
        fun `enabled and disabled placeholders share the same hue`() {
            // ----- Act -----
            val enabled = placeholderInk(
                scheme,
                enabled = true,
            )
            val disabled = placeholderInk(
                scheme,
                enabled = false,
            )

            // ----- Assert -----
            disabled.copy(alpha = 1f) shouldBe enabled.copy(alpha = 1f)
        }
    }

    @Nested
    inner class FitFontSize {
        private val max: TextUnit = 40.sp
        private val floor: TextUnit = 16.sp

        @Test
        fun `returns max when the text fits at max`() {
            // ----- Act -----
            val size = fitFontSize(max, floor, availablePx = 1000) { it.value.toInt() * 10 }

            // ----- Assert -----
            size shouldBe max
        }

        @Test
        fun `returns max when the text fits exactly`() {
            // ----- Act -----
            val size = fitFontSize(max, floor, availablePx = 400) { it.value.toInt() * 10 }

            // ----- Assert -----
            size shouldBe max
        }

        @Test
        fun `shrinks below max when the text is too wide`() {
            // ----- Act -----
            val size = fitFontSize(max, floor, availablePx = 300) { it.value.toInt() * 10 }

            // ----- Assert -----
            (size.value < max.value) shouldBe true
            (size.value * 10 <= 300f) shouldBe true
        }

        @Test
        fun `never goes below the floor`() {
            // ----- Act -----
            val size = fitFontSize(max, floor, availablePx = 10) { it.value.toInt() * 10 }

            // ----- Assert -----
            size shouldBe floor
        }

        @Test
        fun `backs off further when width does not scale linearly`() {
            // ----- Arrange -----
            val widthAt = { size: TextUnit -> (size.value * size.value).toInt() }

            // ----- Act -----
            val size = fitFontSize(
                max,
                floor,
                availablePx = 900,
                widthAt = widthAt,
            )

            // ----- Assert -----
            (size.value < max.value) shouldBe true
            (size.value >= floor.value) shouldBe true
        }
    }
}
