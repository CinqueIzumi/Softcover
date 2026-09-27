package nl.rhaydus.softcover.core.component.header

import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
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
        fun `Label fixtures cover both the default and an explicit letterSpacing`() {
            // ----- Arrange -----
            val expectedLetterSpacings = setOf(TextUnit.Unspecified, 2.2.sp)

            // ----- Act -----
            val coveredLetterSpacings = SectionHeaderUiModel.previews
                .filterIsInstance<SectionHeaderUiModel.Label>()
                .map { it.letterSpacing }
                .toSet()

            // ----- Assert -----
            coveredLetterSpacings shouldBe expectedLetterSpacings
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

        @Test
        fun `Inline tone defaults to Accent`() {
            // ----- Arrange & Act -----
            val model = SectionHeaderUiModel.Inline(eyebrow = "Eyebrow")

            // ----- Assert -----
            model.tone shouldBe SectionHeaderTone.Accent
        }

        @Test
        fun `Inline tone can be set to Error`() {
            // ----- Arrange & Act -----
            val model = SectionHeaderUiModel.Inline(
                eyebrow = "Eyebrow",
                tone = SectionHeaderTone.Error,
            )

            // ----- Assert -----
            model.tone shouldBe SectionHeaderTone.Error
        }

        @Test
        fun `Label letterSpacing defaults to Unspecified`() {
            // ----- Arrange & Act -----
            val model = SectionHeaderUiModel.Label(eyebrow = "Eyebrow")

            // ----- Assert -----
            model.letterSpacing shouldBe TextUnit.Unspecified
        }

        @Test
        fun `Label letterSpacing carries an explicit value`() {
            // ----- Arrange & Act -----
            val model = SectionHeaderUiModel.Label(
                eyebrow = "Eyebrow",
                letterSpacing = 2.2.sp,
            )

            // ----- Assert -----
            model.letterSpacing shouldBe 2.2.sp
        }

        @Test
        fun `Label tone defaults to Muted`() {
            // ----- Arrange & Act -----
            val model = SectionHeaderUiModel.Label(eyebrow = "Eyebrow")

            // ----- Assert -----
            model.tone shouldBe SectionHeaderTone.Muted
        }

        @Test
        fun `Label tone can be set to Accent`() {
            // ----- Arrange & Act -----
            val model = SectionHeaderUiModel.Label(
                eyebrow = "Eyebrow",
                tone = SectionHeaderTone.Accent,
            )

            // ----- Assert -----
            model.tone shouldBe SectionHeaderTone.Accent
        }
    }
}
