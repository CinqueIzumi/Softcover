package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.uibinding.mood.OWN_WORD_CHIP_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItFeltDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowItFeltDraftMapperTest {
    private val tense = BookMood(word = MoodWord.Preset(Mood.Tense))
    private val hopeful = BookMood(
        word = MoodWord.Preset(Mood.Hopeful),
        strength = 60,
    )

    @Nested
    inner class WithChipClicked {
        @Test
        fun `the own word chip opens the own word field`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft()

            // ----- Act -----
            val result = draft.withChipClicked(OWN_WORD_CHIP_KEY)

            // ----- Assert -----
            result shouldBe HowItFeltDraft(ownWordOpen = true)
        }

        @Test
        fun `the own word chip closes the own word field when it is open`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(ownWordOpen = true)

            // ----- Act -----
            val result = draft.withChipClicked(OWN_WORD_CHIP_KEY)

            // ----- Assert -----
            result.ownWordOpen shouldBe false
        }

        @Test
        fun `a preset not yet chosen is added without a strength`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(moods = persistentListOf(hopeful))

            // ----- Act -----
            val result = draft.withChipClicked("tense")

            // ----- Assert -----
            result.moods shouldBe persistentListOf(hopeful, tense)
        }

        @Test
        fun `a preset already chosen is removed`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(moods = persistentListOf(tense, hopeful))

            // ----- Act -----
            val result = draft.withChipClicked("tense")

            // ----- Assert -----
            result.moods shouldBe persistentListOf(hopeful)
        }

        @Test
        fun `an own word chip adds the word and a second tap in other casing removes it`() {
            // ----- Arrange -----
            val added = HowItFeltDraft().withChipClicked("own:Wistful")

            // ----- Act -----
            val result = added.withChipClicked("own:wistful")

            // ----- Assert -----
            added.moods shouldBe persistentListOf(BookMood(word = MoodWord.Own("Wistful")))
            result.moods shouldBe persistentListOf()
        }

        @Test
        fun `an unknown key changes nothing`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(moods = persistentListOf(tense))

            // ----- Act -----
            val result = draft.withChipClicked("not-a-mood")

            // ----- Assert -----
            result shouldBe draft
        }
    }

    @Nested
    inner class WithStrength {
        @Test
        fun `sets the strength of the matching mood only`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(moods = persistentListOf(tense, hopeful))

            // ----- Act -----
            val result = draft.withStrength(
                key = "tense",
                strength = 85,
            )

            // ----- Assert -----
            result.moods shouldBe persistentListOf(tense.copy(strength = 85), hopeful)
        }

        @Test
        fun `a key matching no mood changes nothing`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(moods = persistentListOf(tense))

            // ----- Act -----
            val result = draft.withStrength(
                key = "sad",
                strength = 10,
            )

            // ----- Assert -----
            result shouldBe draft
        }
    }

    @Nested
    inner class WithOwnWordEvent {
        @Test
        fun `typing stores the text`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(ownWordOpen = true)

            // ----- Act -----
            val result = draft.withOwnWordEvent(
                TextFieldEvent.ValueChanged(
                    key = OWN_WORD_CHIP_KEY,
                    text = "Wist",
                ),
            )

            // ----- Assert -----
            result shouldBe HowItFeltDraft(
                ownWordOpen = true,
                ownWordText = "Wist",
            )
        }

        @Test
        fun `submitting adds the word, clears the text and closes the field`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(
                ownWordOpen = true,
                ownWordText = " Wistful ",
            )

            // ----- Act -----
            val result = draft.withOwnWordEvent(TextFieldEvent.Submitted(OWN_WORD_CHIP_KEY))

            // ----- Assert -----
            result shouldBe HowItFeltDraft(
                moods = persistentListOf(BookMood(word = MoodWord.Own("Wistful"))),
                ownWordOpen = false,
                ownWordText = "",
            )
        }

        @Test
        fun `clicking the trailing pill adds the word, clears the text and closes the field`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(
                ownWordOpen = true,
                ownWordText = " Wistful ",
            )

            // ----- Act -----
            val result = draft.withOwnWordEvent(TextFieldEvent.TrailingClicked(OWN_WORD_CHIP_KEY))

            // ----- Assert -----
            result shouldBe HowItFeltDraft(
                moods = persistentListOf(BookMood(word = MoodWord.Own("Wistful"))),
                ownWordOpen = false,
                ownWordText = "",
            )
        }

        @Test
        fun `other events change nothing`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(ownWordText = "Wist")

            // ----- Act -----
            val result = draft.withOwnWordEvent(TextFieldEvent.ShakeEnded(OWN_WORD_CHIP_KEY))

            // ----- Assert -----
            result shouldBe draft
        }
    }

    @Nested
    inner class ToBookMoods {
        @Test
        fun `folds a typed own word in, trimmed`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(
                moods = persistentListOf(tense),
                ownWordText = "  Wistful  ",
            )

            // ----- Act -----
            val result = draft.toBookMoods()

            // ----- Assert -----
            result shouldBe listOf(tense, BookMood(word = MoodWord.Own("Wistful")))
        }

        @Test
        fun `keeps the first spelling when the typed word matches a chosen one ignoring case`() {
            // ----- Arrange -----
            val chosen = BookMood(
                word = MoodWord.Own("Wistful"),
                strength = 40,
            )

            val draft = HowItFeltDraft(
                moods = persistentListOf(chosen),
                ownWordText = "wistful",
            )

            // ----- Act -----
            val result = draft.toBookMoods()

            // ----- Assert -----
            result shouldBe listOf(chosen)
        }

        @Test
        fun `ignores a whitespace-only own word`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(
                moods = persistentListOf(tense),
                ownWordText = "   ",
            )

            // ----- Act -----
            val result = draft.toBookMoods()

            // ----- Assert -----
            result shouldBe listOf(tense)
        }
    }
}
