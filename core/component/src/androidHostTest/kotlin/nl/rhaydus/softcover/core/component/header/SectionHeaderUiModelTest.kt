package nl.rhaydus.softcover.core.component.header

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SectionHeaderUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `every register subtype appears in the fixtures`() {
            // ----- Arrange -----
            val expectedRegisters = setOf(
                SectionHeaderUiModel.Section::class,
                SectionHeaderUiModel.Inline::class,
                SectionHeaderUiModel.Label::class,
            )

            // ----- Act -----
            val coveredRegisters = SectionHeaderUiModel.previews.map { it::class }.toSet()

            // ----- Assert -----
            coveredRegisters shouldBe expectedRegisters
        }

        @Test
        fun `Section fixtures cover both with and without a headline`() {
            // ----- Arrange -----
            val expectedHeadlinePresence = setOf(true, false)

            // ----- Act -----
            val coveredHeadlinePresence = SectionHeaderUiModel.previews
                .filterIsInstance<SectionHeaderUiModel.Section>()
                .map { it.headline != null }
                .toSet()

            // ----- Assert -----
            coveredHeadlinePresence shouldBe expectedHeadlinePresence
        }

        @Test
        fun `every Section fixture leaves pulseKey at its default`() {
            // ----- Act -----
            val pulseKeys = SectionHeaderUiModel.previews
                .filterIsInstance<SectionHeaderUiModel.Section>()
                .map { it.pulseKey }
                .toSet()

            // ----- Assert -----
            pulseKeys shouldBe setOf(0)
        }

        @Test
        fun `Label fixtures cover both the default Muted tone and an explicit Accent tone`() {
            // ----- Arrange -----
            val expectedTones = setOf(SectionHeaderTone.Muted, SectionHeaderTone.Accent)

            // ----- Act -----
            val coveredTones = SectionHeaderUiModel.previews
                .filterIsInstance<SectionHeaderUiModel.Label>()
                .map { it.tone }
                .toSet()

            // ----- Assert -----
            coveredTones shouldBe expectedTones
        }

        @Test
        fun `Inline fixtures cover both Accent and Error tones`() {
            // ----- Arrange -----
            val expectedTones = setOf(SectionHeaderTone.Accent, SectionHeaderTone.Error)

            // ----- Act -----
            val coveredTones = SectionHeaderUiModel.previews
                .filterIsInstance<SectionHeaderUiModel.Inline>()
                .map { it.tone }
                .toSet()

            // ----- Assert -----
            coveredTones shouldBe expectedTones
        }
    }
}
