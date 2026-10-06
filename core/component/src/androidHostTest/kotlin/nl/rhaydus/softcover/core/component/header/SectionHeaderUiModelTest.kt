package nl.rhaydus.softcover.core.component.header

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
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
            val coveredRegisters = SectionHeaderUiModel.previews.values.map { it::class }.toSet()

            // ----- Assert -----
            coveredRegisters shouldBe expectedRegisters
        }

        @Test
        fun `Section fixtures cover both with and without a headline`() {
            // ----- Arrange -----
            val expectedHeadlinePresence = setOf(true, false)

            // ----- Act -----
            val coveredHeadlinePresence = SectionHeaderUiModel.previews.values
                .filterIsInstance<SectionHeaderUiModel.Section>()
                .map { it.headline != null }
                .toSet()

            // ----- Assert -----
            coveredHeadlinePresence shouldBe expectedHeadlinePresence
        }

        @Test
        fun `Label fixtures cover both the default Muted tone and an explicit Accent tone`() {
            // ----- Arrange -----
            val expectedTones = setOf(SectionHeaderTone.Muted, SectionHeaderTone.Accent)

            // ----- Act -----
            val coveredTones = SectionHeaderUiModel.previews.values
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
            val coveredTones = SectionHeaderUiModel.previews.values
                .filterIsInstance<SectionHeaderUiModel.Inline>()
                .map { it.tone }
                .toSet()

            // ----- Assert -----
            coveredTones shouldBe expectedTones
        }
    }

    @Nested
    inner class SectionConstructors {
        @Test
        fun `string constructor wraps headline and description as single plain segments`() {
            // ----- Arrange -----
            val eyebrow = "Lists"
            val headline = "Halfway"
            val description = "Almost there."

            // ----- Act -----
            val section = SectionHeaderUiModel.Section(
                eyebrow = eyebrow,
                headline = headline,
                description = description,
            )

            // ----- Assert -----
            section.headline shouldBe persistentListOf(HeaderTextSegment.Plain(text = "Halfway"))
            section.description shouldBe persistentListOf(HeaderTextSegment.Plain(text = "Almost there."))
        }

        @Test
        fun `string constructor leaves absent headline and description null`() {
            // ----- Arrange -----
            val eyebrow = "Lists"

            // ----- Act -----
            val section = SectionHeaderUiModel.Section(eyebrow = eyebrow)

            // ----- Assert -----
            section.headline shouldBe null
            section.description shouldBe null
        }

        @Test
        fun `headlineText joins every segment without emphasis`() {
            // ----- Arrange -----
            val section = SectionHeaderUiModel.Section(
                eyebrow = "Lists",
                headline = persistentListOf(
                    HeaderTextSegment.Plain(text = "Shelve "),
                    HeaderTextSegment.Emphasised(text = "Piranesi"),
                ),
                description = null,
            )

            // ----- Act & Assert -----
            section.headlineText shouldBe "Shelve Piranesi"
        }

        @Test
        fun `headlineText is null without a headline`() {
            // ----- Arrange -----
            val eyebrow = "Lists"

            // ----- Act & Assert -----
            SectionHeaderUiModel.Section(eyebrow = eyebrow).headlineText shouldBe null
        }
    }

    @Nested
    inner class EmphasisFixtures {
        @Test
        fun `a Section fixture carries an emphasised headline run`() {
            // ----- Arrange -----
            val fixtures = SectionHeaderUiModel.previews.values

            // ----- Act -----
            val hasEmphasis = fixtures
                .filterIsInstance<SectionHeaderUiModel.Section>()
                .any { section -> section.headline.orEmpty().any { it is HeaderTextSegment.Emphasised } }

            // ----- Assert -----
            hasEmphasis shouldBe true
        }
    }
}
