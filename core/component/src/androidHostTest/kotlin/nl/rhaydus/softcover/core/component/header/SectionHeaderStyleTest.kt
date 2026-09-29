package nl.rhaydus.softcover.core.component.header

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
}
