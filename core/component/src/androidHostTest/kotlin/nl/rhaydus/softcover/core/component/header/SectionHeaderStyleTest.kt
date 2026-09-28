package nl.rhaydus.softcover.core.component.header

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SectionHeaderStyleTest {
    @Nested
    inner class PulseEnvelope {
        @Test
        fun `progress 0 is at the trough`() {
            // ----- Act -----
            val envelope = sectionHeaderPulseEnvelope(progress = 0f)

            // ----- Assert -----
            envelope shouldBe 0f
        }

        @Test
        fun `progress 0_25 is halfway to the peak`() {
            // ----- Act -----
            val envelope = sectionHeaderPulseEnvelope(progress = 0.25f)

            // ----- Assert -----
            envelope shouldBe 0.5f
        }

        @Test
        fun `progress 0_5 is at the peak`() {
            // ----- Act -----
            val envelope = sectionHeaderPulseEnvelope(progress = 0.5f)

            // ----- Assert -----
            envelope shouldBe 1f
        }

        @Test
        fun `progress 1 is back at the trough`() {
            // ----- Act -----
            val envelope = sectionHeaderPulseEnvelope(progress = 1f)

            // ----- Assert -----
            envelope shouldBe 0f
        }

        @Test
        fun `progress below 0 clamps to 0`() {
            // ----- Act -----
            val envelope = sectionHeaderPulseEnvelope(progress = -0.5f)

            // ----- Assert -----
            envelope shouldBe 0f
        }

        @Test
        fun `progress above 1 clamps to 0`() {
            // ----- Act -----
            val envelope = sectionHeaderPulseEnvelope(progress = 1.5f)

            // ----- Assert -----
            envelope shouldBe 0f
        }
    }

    @Nested
    inner class ToneColor {
        // region Fixtures
        /** Every role gets a distinct colour so a resolved colour can only match its intended role. */
        private val colorScheme = lightColorScheme(
            primary = Color(color = 0xFF111111),
            error = Color(color = 0xFF222222),
            onSurfaceVariant = Color(color = 0xFF333333),
        )
        // endregion
        @Test
        fun `Accent resolves to the primary colour`() {
            // ----- Act -----
            val color = sectionHeaderToneColor(
                tone = SectionHeaderTone.Accent,
                colorScheme = colorScheme,
            )

            // ----- Assert -----
            color shouldBe colorScheme.primary
        }

        @Test
        fun `Error resolves to the error colour`() {
            // ----- Act -----
            val color = sectionHeaderToneColor(
                tone = SectionHeaderTone.Error,
                colorScheme = colorScheme,
            )

            // ----- Assert -----
            color shouldBe colorScheme.error
        }

        @Test
        fun `Muted resolves to the onSurfaceVariant colour`() {
            // ----- Act -----
            val color = sectionHeaderToneColor(
                tone = SectionHeaderTone.Muted,
                colorScheme = colorScheme,
            )

            // ----- Assert -----
            color shouldBe colorScheme.onSurfaceVariant
        }

        @Test
        fun `every tone resolves a distinct colour`() {
            // ----- Act -----
            val colors = SectionHeaderTone.entries.map {
                sectionHeaderToneColor(
                    tone = it,
                    colorScheme = colorScheme,
                )
            }.toSet()

            // ----- Assert -----
            colors.size shouldBe SectionHeaderTone.entries.size
        }
    }
}
