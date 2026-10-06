package nl.rhaydus.softcover.core.component.chip

import androidx.compose.material3.lightColorScheme
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChipToneStyleTest {
    private val scheme = lightColorScheme()

    @Nested
    inner class Tonal {
        @Test
        fun `selected and unselected pick different container and ink`() {
            // ----- Arrange -----
            val tone = ChipTone.Tonal

            // ----- Act -----
            val selected = tone.style(
                scheme,
                selected = true,
            )
            val unselected = tone.style(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            (selected.container == unselected.container) shouldBe false
            (selected.ink == unselected.ink) shouldBe false
        }
    }

    @Nested
    inner class Choice {
        @Test
        fun `selected differs from unselected`() {
            // ----- Arrange -----
            val tone = ChipTone.Choice

            // ----- Act -----
            val selected = tone.style(
                scheme,
                selected = true,
            )
            val unselected = tone.style(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            (selected.container == unselected.container) shouldBe false
        }
    }
}
