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
    }

    @Nested
    inner class Defaults {
        @Test
        fun `Section pulseKey defaults to 0`() {
            // ----- Arrange & Act -----
            val model = SectionHeaderUiModel.Section(eyebrow = "Eyebrow")

            // ----- Assert -----
            model.pulseKey shouldBe 0
        }

        @Test
        fun `Section headline defaults to null`() {
            // ----- Arrange & Act -----
            val model = SectionHeaderUiModel.Section(eyebrow = "Eyebrow")

            // ----- Assert -----
            model.headline shouldBe null
        }

        @Test
        fun `Section description defaults to null`() {
            // ----- Arrange & Act -----
            val model = SectionHeaderUiModel.Section(eyebrow = "Eyebrow")

            // ----- Assert -----
            model.description shouldBe null
        }
    }
}
