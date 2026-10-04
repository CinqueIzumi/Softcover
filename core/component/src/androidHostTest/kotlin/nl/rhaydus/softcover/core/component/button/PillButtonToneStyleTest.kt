package nl.rhaydus.softcover.core.component.button

import androidx.compose.material3.lightColorScheme
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PillButtonToneStyleTest {
    private val scheme = lightColorScheme()

    @Nested
    inner class Border {
        @Test
        fun `only the Outlined tone has a border`() {
            // ----- Act -----
            val bordered = PillButtonTone.entries.filter { it.style(scheme).border != null }

            // ----- Assert -----
            bordered shouldBe listOf(PillButtonTone.Outlined)
        }

        @Test
        fun `Outlined carries a border`() {
            // ----- Act -----
            val border = PillButtonTone.Outlined.style(scheme).border

            // ----- Assert -----
            border.shouldNotBeNull()
        }

        @Test
        fun `Tonal has no border`() {
            // ----- Act -----
            val border = PillButtonTone.Tonal.style(scheme).border

            // ----- Assert -----
            border.shouldBeNull()
        }
    }

    @Nested
    inner class IconTint {
        @Test
        fun `every tone except Outlined tints its icon like its ink`() {
            // ----- Act -----
            val tones = PillButtonTone.entries.filter { it != PillButtonTone.Outlined }

            // ----- Assert -----
            tones.forEach { tone ->
                val style = tone.style(scheme)
                style.iconTint shouldBe style.ink
            }
        }

        @Test
        fun `Outlined tints its icon differently from its ink`() {
            // ----- Act -----
            val style = PillButtonTone.Outlined.style(scheme)

            // ----- Assert -----
            (style.iconTint == style.ink) shouldBe false
        }
    }
}
