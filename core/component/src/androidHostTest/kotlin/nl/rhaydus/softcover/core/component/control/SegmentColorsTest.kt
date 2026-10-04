package nl.rhaydus.softcover.core.component.control

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SegmentColorsTest {
    private val scheme = lightColorScheme()

    @Nested
    inner class SegmentColorsFor {
        @Test
        fun `a selected segment fills its container`() {
            // ----- Act -----
            val colors = segmentColors(
                scheme,
                selected = true,
            )

            // ----- Assert -----
            (colors.container == Color.Transparent) shouldBe false
        }

        @Test
        fun `an unselected segment has no container`() {
            // ----- Act -----
            val colors = segmentColors(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            colors.container shouldBe Color.Transparent
        }

        @Test
        fun `selected and unselected segments pick different content colours`() {
            // ----- Act -----
            val selected = segmentColors(
                scheme,
                selected = true,
            )
            val unselected = segmentColors(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            (selected.content == unselected.content) shouldBe false
        }
    }

    @Nested
    inner class ProseWordColorsFor {
        @Test
        fun `a selected word is underlined`() {
            // ----- Act -----
            val colors = proseWordColors(
                scheme,
                selected = true,
            )

            // ----- Assert -----
            (colors.underline == Color.Transparent) shouldBe false
            colors.underline shouldBe colors.word
        }

        @Test
        fun `an unselected word has no underline`() {
            // ----- Act -----
            val colors = proseWordColors(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            colors.underline shouldBe Color.Transparent
        }

        @Test
        fun `selected and unselected words pick different colours`() {
            // ----- Act -----
            val selected = proseWordColors(
                scheme,
                selected = true,
            )
            val unselected = proseWordColors(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            (selected.word == unselected.word) shouldBe false
        }
    }
}
