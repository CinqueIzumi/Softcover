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
            // ----- Act -----
            val selected = ChipTone.Tonal.style(
                scheme,
                selected = true,
            )
            val unselected = ChipTone.Tonal.style(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            (selected.container == unselected.container) shouldBe false
            (selected.ink == unselected.ink) shouldBe false
        }

        @Test
        fun `selecting does not change the font weight`() {
            // ----- Act -----
            val selected = ChipTone.Tonal.style(
                scheme,
                selected = true,
            )
            val unselected = ChipTone.Tonal.style(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            selected.weight shouldBe unselected.weight
        }
    }

    @Nested
    inner class Choice {
        @Test
        fun `unselected looks exactly like an unselected Tonal chip`() {
            // ----- Act -----
            val choice = ChipTone.Choice.style(
                scheme,
                selected = false,
            )
            val tonal = ChipTone.Tonal.style(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            choice shouldBe tonal
        }

        @Test
        fun `selected differs from unselected`() {
            // ----- Act -----
            val selected = ChipTone.Choice.style(
                scheme,
                selected = true,
            )
            val unselected = ChipTone.Choice.style(
                scheme,
                selected = false,
            )

            // ----- Assert -----
            (selected.container == unselected.container) shouldBe false
        }
    }
}
