package nl.rhaydus.softcover.core.component.control

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IconToggleStyleTest {
    private val scheme = lightColorScheme()

    @Nested
    inner class Filled {
        @Test
        fun `checked and unchecked pick different container and tint`() {
            // ----- Act -----
            val checked = ToggleIconStyle.Filled.style(
                scheme,
                checked = true,
            )
            val unchecked = ToggleIconStyle.Filled.style(
                scheme,
                checked = false,
            )

            // ----- Assert -----
            (checked.container == unchecked.container) shouldBe false
            (checked.tint == unchecked.tint) shouldBe false
        }

        @Test
        fun `checked tint contrasts with its own container`() {
            // ----- Act -----
            val checked = ToggleIconStyle.Filled.style(
                scheme,
                checked = true,
            )

            // ----- Assert -----
            checked.container shouldBe scheme.primary
            checked.tint shouldBe scheme.onPrimary
        }

        @Test
        fun `has no tooltip`() {
            // ----- Act -----
            val style = ToggleIconStyle.Filled.style(
                scheme,
                checked = false,
            )

            // ----- Assert -----
            style.hasTooltip shouldBe false
        }
    }

    @Nested
    inner class Bare {
        @Test
        fun `never paints a container`() {
            // ----- Act -----
            val checked = ToggleIconStyle.Bare.style(
                scheme,
                checked = true,
            )
            val unchecked = ToggleIconStyle.Bare.style(
                scheme,
                checked = false,
            )

            // ----- Assert -----
            checked.container shouldBe Color.Transparent
            unchecked.container shouldBe Color.Transparent
        }

        @Test
        fun `only the tint changes with the checked state`() {
            // ----- Act -----
            val checked = ToggleIconStyle.Bare.style(
                scheme,
                checked = true,
            )
            val unchecked = ToggleIconStyle.Bare.style(
                scheme,
                checked = false,
            )

            // ----- Assert -----
            (checked.tint == unchecked.tint) shouldBe false
            checked.size shouldBe unchecked.size
            checked.iconSize shouldBe unchecked.iconSize
        }

        @Test
        fun `has a tooltip`() {
            // ----- Act -----
            val style = ToggleIconStyle.Bare.style(
                scheme,
                checked = true,
            )

            // ----- Assert -----
            style.hasTooltip shouldBe true
        }
    }
}
