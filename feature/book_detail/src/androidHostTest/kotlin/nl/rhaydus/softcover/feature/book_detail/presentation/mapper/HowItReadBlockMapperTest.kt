package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.traits.HowItReadBlockUiModel
import nl.rhaydus.softcover.core.component.traits.HowItReadRowUiModel
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.TraitAnswer
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowItReadBlockMapperTest {
    private fun BookTraits.filled(expanded: Boolean = false): HowItReadBlockUiModel.Filled =
        toHowItReadBlock(expanded = expanded).shouldBeInstanceOf<HowItReadBlockUiModel.Filled>()

    @Nested
    inner class Empty {
        @Test
        fun `maps empty traits to the empty block`() {
            // ----- Arrange -----
            val traits = BookTraits(bookId = 1)

            // ----- Act -----
            val result = traits.toHowItReadBlock(expanded = true)

            // ----- Assert -----
            result.shouldBeInstanceOf<HowItReadBlockUiModel.Empty>()
        }
    }

    @Nested
    inner class Rows {
        @Test
        fun `lists only the answered dimensions`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                pace = Pace.SLOW,
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.rows.map { it.label } shouldBe listOf("Pace")
        }

        @Test
        fun `orders the rows pace, plot or character, characters, complicated, writing`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                pace = Pace.MODERATE,
                plotCharacterBalance = 50,
                characters = mapOf(
                    CharacterTrait.LIKEABLE to TraitAnswer.YES,
                    CharacterTrait.BIPOC to TraitAnswer.COMPLICATED,
                ),
                writingStyle = WritingStyle.SIMPLE,
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.rows.map { it.label } shouldBe listOf(
                "Pace",
                "Plot or character",
                "Characters",
                "Complicated",
                "Writing",
            )
        }

        @Test
        fun `gives the plot or character row a scale position`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                plotCharacterBalance = 78,
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            val row = result.rows.single().shouldBeInstanceOf<HowItReadRowUiModel.Scale>()
            row.position shouldBe 0.78f
            row.value shouldBe "Mostly character"
        }

        @Test
        fun `lists the yes and complicated characters on separate rows`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                characters = mapOf(
                    CharacterTrait.LIKEABLE to TraitAnswer.YES,
                    CharacterTrait.BELIEVABLE to TraitAnswer.COMPLICATED,
                    CharacterTrait.WELL_DEVELOPED to TraitAnswer.YES,
                ),
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.rows.map { it.value } shouldBe listOf("Likeable, well developed", "Believable")
        }

        @Test
        fun `writes the style before the flags`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                writingStyle = WritingStyle.DEMANDING,
                writingFlags = setOf(WritingFlag.POETIC, WritingFlag.VIVID),
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.rows.single().value shouldBe "Demanding, vivid, poetic"
        }

        @Test
        fun `writes flags alone when there is no style`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                writingFlags = setOf(WritingFlag.UNCONVENTIONAL),
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.rows.single().value shouldBe "Unconventional"
        }

        @Test
        fun `capitalises the first word, lowercases the rest and keeps acronyms`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                characters = mapOf(
                    CharacterTrait.WELL_DEVELOPED to TraitAnswer.YES,
                    CharacterTrait.MULTI_LAYERED to TraitAnswer.YES,
                    CharacterTrait.BIPOC to TraitAnswer.YES,
                    CharacterTrait.LGBTQ to TraitAnswer.YES,
                ),
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.rows.single().value shouldBe "Well developed, multi-layered, BIPOC, LGBTQ+"
        }

        @Test
        fun `keeps an acronym as the first word`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                characters = mapOf(CharacterTrait.LGBTQ to TraitAnswer.YES),
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.rows.single().value shouldBe "LGBTQ+"
        }
    }

    @Nested
    inner class BalanceBuckets {
        private fun bucketOf(balance: Int): String {
            val traits = BookTraits(
                bookId = 1,
                plotCharacterBalance = balance,
            )

            return traits.filled().rows.single().value
        }

        @Test
        fun `buckets the balance at its boundaries`() {
            // ----- Arrange -----
            val expected = mapOf(
                0 to "Plot-driven",
                20 to "Plot-driven",
                21 to "Mostly plot",
                40 to "Mostly plot",
                41 to "A mix",
                59 to "A mix",
                60 to "Mostly character",
                79 to "Mostly character",
                80 to "Character-driven",
                100 to "Character-driven",
            )

            // ----- Act -----
            val result = expected.keys.associateWith { bucketOf(it) }

            // ----- Assert -----
            result shouldBe expected
        }
    }

    @Nested
    inner class Summary {
        @Test
        fun `joins pace, balance bucket and style`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                pace = Pace.VARIABLE,
                plotCharacterBalance = 78,
                writingStyle = WritingStyle.DEMANDING,
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.summary shouldBe "Variable pace, mostly character, demanding writing"
        }

        @Test
        fun `skips the parts that are not answered`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                pace = Pace.FAST,
                writingStyle = WritingStyle.SIMPLE,
                characters = mapOf(CharacterTrait.LIKEABLE to TraitAnswer.YES),
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.summary shouldBe "Fast pace, simple writing"
        }

        @Test
        fun `falls back to the yes characters when pace, balance and style are unanswered`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                characters = mapOf(
                    CharacterTrait.LIKEABLE to TraitAnswer.YES,
                    CharacterTrait.BELIEVABLE to TraitAnswer.YES,
                    CharacterTrait.BIPOC to TraitAnswer.COMPLICATED,
                ),
                writingFlags = setOf(WritingFlag.VIVID),
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.summary shouldBe "Likeable, believable"
        }

        @Test
        fun `falls back to the complicated characters when there are no yes characters`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                characters = mapOf(CharacterTrait.WELL_DEVELOPED to TraitAnswer.COMPLICATED),
                writingFlags = setOf(WritingFlag.VIVID),
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.summary shouldBe "Well developed"
        }

        @Test
        fun `falls back to the flags when only flags are answered`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                writingFlags = setOf(WritingFlag.VIVID, WritingFlag.POETIC),
            )

            // ----- Act -----
            val result = traits.filled()

            // ----- Assert -----
            result.summary shouldBe "Vivid, poetic"
        }
    }
}
