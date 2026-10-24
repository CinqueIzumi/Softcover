package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.CharacterTrait
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.TraitAnswer
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadAnswer
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItReadDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowItReadDraftMapperTest {
    @Nested
    inner class WithChipClicked {
        @Test
        fun `selects a tapped pace chip`() {
            // ----- Arrange -----
            val draft = HowItReadDraft()

            // ----- Act -----
            val result = draft.withChipClicked(Pace.FAST.toKey())

            // ----- Assert -----
            result.paceKey shouldBe Pace.FAST.toKey()
        }

        @Test
        fun `replaces the selected pace when a different pace chip is tapped`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(paceKey = Pace.SLOW.toKey())

            // ----- Act -----
            val result = draft.withChipClicked(Pace.FAST.toKey())

            // ----- Assert -----
            result.paceKey shouldBe Pace.FAST.toKey()
        }

        @Test
        fun `clears the pace when the selected pace chip is tapped again`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(paceKey = Pace.SLOW.toKey())

            // ----- Act -----
            val result = draft.withChipClicked(Pace.SLOW.toKey())

            // ----- Assert -----
            result.paceKey shouldBe null
        }

        @Test
        fun `replaces the selected writing style when a different style chip is tapped`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(writingStyleKey = WritingStyle.SIMPLE.toKey())

            // ----- Act -----
            val result = draft.withChipClicked(WritingStyle.DEMANDING.toKey())

            // ----- Assert -----
            result.writingStyleKey shouldBe WritingStyle.DEMANDING.toKey()
        }

        @Test
        fun `clears the writing style when the selected style chip is tapped again`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(writingStyleKey = WritingStyle.SIMPLE.toKey())

            // ----- Act -----
            val result = draft.withChipClicked(WritingStyle.SIMPLE.toKey())

            // ----- Assert -----
            result.writingStyleKey shouldBe null
        }

        @Test
        fun `answers an unanswered character chip yes`() {
            // ----- Arrange -----
            val draft = HowItReadDraft()

            // ----- Act -----
            val result = draft.withChipClicked(CharacterTrait.LIKEABLE.toKey())

            // ----- Assert -----
            result.characters shouldBe persistentMapOf(CharacterTrait.LIKEABLE.toKey() to HowItReadAnswer.YES)
        }

        @Test
        fun `turns a yes character chip into complicated`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(
                characters = persistentMapOf(CharacterTrait.LIKEABLE.toKey() to HowItReadAnswer.YES),
            )

            // ----- Act -----
            val result = draft.withChipClicked(CharacterTrait.LIKEABLE.toKey())

            // ----- Assert -----
            result.characters shouldBe persistentMapOf(
                CharacterTrait.LIKEABLE.toKey() to HowItReadAnswer.COMPLICATED,
            )
        }

        @Test
        fun `clears a complicated character chip`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(
                characters = persistentMapOf(
                    CharacterTrait.LIKEABLE.toKey() to HowItReadAnswer.COMPLICATED,
                    CharacterTrait.BIPOC.toKey() to HowItReadAnswer.YES,
                ),
            )

            // ----- Act -----
            val result = draft.withChipClicked(CharacterTrait.LIKEABLE.toKey())

            // ----- Assert -----
            result.characters shouldBe persistentMapOf(CharacterTrait.BIPOC.toKey() to HowItReadAnswer.YES)
        }

        @Test
        fun `toggles a writing flag on and off without touching the style`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(writingStyleKey = WritingStyle.SIMPLE.toKey())

            // ----- Act -----
            val on = draft.withChipClicked(WritingFlag.POETIC.toKey())
            val off = on.withChipClicked(WritingFlag.POETIC.toKey())

            // ----- Assert -----
            on.flagKeys shouldBe persistentSetOf(WritingFlag.POETIC.toKey())
            off.flagKeys shouldBe persistentSetOf()
            off.writingStyleKey shouldBe WritingStyle.SIMPLE.toKey()
        }

        @Test
        fun `ignores an unknown chip key`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(paceKey = Pace.SLOW.toKey())

            // ----- Act -----
            val result = draft.withChipClicked("unknown")

            // ----- Assert -----
            result shouldBe draft
        }
    }

    @Nested
    inner class WithBalance {
        @Test
        fun `sets the balance`() {
            // ----- Arrange -----
            val draft = HowItReadDraft()

            // ----- Act -----
            val result = draft.withBalance(0.3f)

            // ----- Assert -----
            result.balance shouldBe 0.3f
        }

        @Test
        fun `clears the balance when given null`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(balance = 0.3f)

            // ----- Act -----
            val result = draft.withBalance(null)

            // ----- Assert -----
            result.balance shouldBe null
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `a fully answered book survives the trip through the draft`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 7,
                pace = Pace.VARIABLE,
                plotCharacterBalance = 78,
                characters = CharacterTrait.entries.associateWith { TraitAnswer.YES },
                writingStyle = WritingStyle.DEMANDING,
                writingFlags = WritingFlag.entries.toSet(),
            )

            // ----- Act -----
            val result = traits.toHowItReadDraft().toBookTraits(bookId = 7)

            // ----- Assert -----
            result shouldBe traits
        }

        @Test
        fun `every pace, style and trait answer round trips`() {
            // ----- Arrange -----
            val paces = Pace.entries.map { BookTraits(bookId = 1).copy(pace = it) }
            val styles = WritingStyle.entries.map { BookTraits(bookId = 1).copy(writingStyle = it) }
            val complicated = CharacterTrait.entries.map {
                BookTraits(
                    bookId = 1,
                    characters = mapOf(it to TraitAnswer.COMPLICATED),
                )
            }
            val all = paces + styles + complicated

            // ----- Act -----
            val result = all.map { it.toHowItReadDraft().toBookTraits(bookId = 1) }

            // ----- Assert -----
            result shouldBe all
        }

        @Test
        fun `turns the balance into a fraction`() {
            // ----- Arrange -----
            val traits = BookTraits(
                bookId = 1,
                plotCharacterBalance = 78,
            )

            // ----- Act -----
            val result = traits.toHowItReadDraft()

            // ----- Assert -----
            result.balance shouldBe 0.78f
        }

        @Test
        fun `rounds a dragged balance to a whole percent`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(balance = 0.784f)

            // ----- Act -----
            val result = draft.toBookTraits(bookId = 1)

            // ----- Assert -----
            result.plotCharacterBalance shouldBe 78
        }

        @Test
        fun `keeps the balance ends at 0 and 100`() {
            // ----- Arrange -----
            val plot = HowItReadDraft(balance = 0f)
            val character = HowItReadDraft(balance = 1f)

            // ----- Act -----
            val plotResult = plot.toBookTraits(bookId = 1)
            val characterResult = character.toBookTraits(bookId = 1)

            // ----- Assert -----
            plotResult.plotCharacterBalance shouldBe 0
            characterResult.plotCharacterBalance shouldBe 100
        }

        @Test
        fun `maps an empty draft to empty traits`() {
            // ----- Arrange -----
            val draft = HowItReadDraft()

            // ----- Act -----
            val result = draft.toBookTraits(bookId = 5)

            // ----- Assert -----
            result shouldBe BookTraits(bookId = 5)
        }

        @Test
        fun `drops draft keys that match no trait`() {
            // ----- Arrange -----
            val draft = HowItReadDraft(
                paceKey = "bogus",
                characters = persistentMapOf("bogus" to HowItReadAnswer.YES),
                flagKeys = persistentSetOf("bogus"),
            )

            // ----- Act -----
            val result = draft.toBookTraits(bookId = 5)

            // ----- Assert -----
            result shouldBe BookTraits(bookId = 5)
        }
    }
}
