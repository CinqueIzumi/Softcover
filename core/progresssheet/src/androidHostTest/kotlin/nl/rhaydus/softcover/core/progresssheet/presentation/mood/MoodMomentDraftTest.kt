package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.field.TextFieldEvent
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.uibinding.mood.OWN_WORD_CHIP_KEY
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodMomentDraftTest {
    private val draft = MoodMomentDraft.adding(
        PendingMoodMoment(
            bookId = 7,
            page = 120,
            seconds = null,
            percent = 40,
        ),
    )

    @Nested
    inner class CommittedWord {
        @Test
        fun `is the trimmed own word when text is typed`() {
            // ----- Arrange -----
            val start = draft.copy(ownWordText = "  Giddy ")

            // ----- Act -----
            val result = start.committedWord

            // ----- Assert -----
            result shouldBe MoodWord.Own("Giddy")
        }

        @Test
        fun `typed text wins over a chosen word`() {
            // ----- Arrange -----
            val start = draft.copy(
                word = MoodWord.Preset(Mood.Tense),
                ownWordText = "Giddy",
            )

            // ----- Act -----
            val result = start.committedWord

            // ----- Assert -----
            result shouldBe MoodWord.Own("Giddy")
        }

        @Test
        fun `blank text falls back to the chosen word`() {
            // ----- Arrange -----
            val start = draft.copy(
                word = MoodWord.Preset(Mood.Tense),
                ownWordText = "   ",
            )

            // ----- Act -----
            val result = start.committedWord

            // ----- Assert -----
            result shouldBe MoodWord.Preset(Mood.Tense)
        }

        @Test
        fun `is null with blank text and no word`() {
            // ----- Arrange -----
            val start = draft.copy(ownWordText = "  ")

            // ----- Act -----
            val result = start.committedWord

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class WithChipClicked {
        @Test
        fun `the own-word chip toggles the own-word field open and closed`() {
            // ----- Arrange -----
            val start = draft

            // ----- Act -----
            val opened = start.withChipClicked(OWN_WORD_CHIP_KEY)
            val closed = opened.withChipClicked(OWN_WORD_CHIP_KEY)

            // ----- Assert -----
            opened.ownWordOpen shouldBe true
            closed.ownWordOpen shouldBe false
        }

        @Test
        fun `a preset chip selects that single word`() {
            // ----- Arrange -----
            val start = draft.withChipClicked("tense")

            // ----- Act -----
            val result = start.withChipClicked("gripped")

            // ----- Assert -----
            result.word shouldBe MoodWord.Preset(Mood.Gripped)
        }

        @Test
        fun `tapping the selected preset again clears the word`() {
            // ----- Arrange -----
            val start = draft.withChipClicked("tense")

            // ----- Act -----
            val result = start.withChipClicked("tense")

            // ----- Assert -----
            result.word.shouldBeNull()
        }

        @Test
        fun `an own-word chip selects that own word`() {
            // ----- Arrange -----
            val start = draft

            // ----- Act -----
            val result = start.withChipClicked("own:Giddy")

            // ----- Assert -----
            result.word shouldBe MoodWord.Own("Giddy")
        }

        @Test
        fun `tapping the selected own word again in another case clears it`() {
            // ----- Arrange -----
            val start = draft.withChipClicked("own:Giddy")

            // ----- Act -----
            val result = start.withChipClicked("own:giddy")

            // ----- Assert -----
            result.word.shouldBeNull()
        }

        @Test
        fun `selecting a chip discards typed own-word text`() {
            // ----- Arrange -----
            val start = draft.copy(ownWordText = "Giddy")

            // ----- Act -----
            val result = start.withChipClicked("tense")

            // ----- Assert -----
            result.ownWordText shouldBe ""
            result.committedWord shouldBe MoodWord.Preset(Mood.Tense)
        }

        @Test
        fun `an unknown key leaves the draft unchanged`() {
            // ----- Arrange -----
            val start = draft.copy(
                word = MoodWord.Preset(Mood.Tense),
                ownWordText = "x",
            )

            // ----- Act -----
            val unknown = start.withChipClicked("nonsense")
            val blankOwn = start.withChipClicked("own:   ")

            // ----- Assert -----
            unknown shouldBe start
            blankOwn shouldBe start
        }
    }

    @Nested
    inner class WithOwnWordEvent {
        @Test
        fun `typing text stores it and drops the chosen word`() {
            // ----- Arrange -----
            val start = draft.copy(word = MoodWord.Preset(Mood.Tense))

            // ----- Act -----
            val result = start.withOwnWordEvent(
                TextFieldEvent.ValueChanged(
                    key = "own-word",
                    text = "Gid",
                ),
            )

            // ----- Assert -----
            result.ownWordText shouldBe "Gid"
            result.word.shouldBeNull()
        }

        @Test
        fun `typing blank text keeps the chosen word`() {
            // ----- Arrange -----
            val start = draft.copy(word = MoodWord.Preset(Mood.Tense))

            // ----- Act -----
            val result = start.withOwnWordEvent(
                TextFieldEvent.ValueChanged(
                    key = "own-word",
                    text = "  ",
                ),
            )

            // ----- Assert -----
            result.word shouldBe MoodWord.Preset(Mood.Tense)
        }

        @Test
        fun `submitting commits the trimmed text as the word and closes the field`() {
            // ----- Arrange -----
            val start = draft.copy(
                ownWordText = " Giddy ",
                ownWordOpen = true,
            )

            // ----- Act -----
            val result = start.withOwnWordEvent(TextFieldEvent.Submitted(key = "own-word"))

            // ----- Assert -----
            result.word shouldBe MoodWord.Own("Giddy")
            result.ownWordText shouldBe ""
            result.ownWordOpen shouldBe false
        }

        @Test
        fun `submitting blank text keeps the chosen word`() {
            // ----- Arrange -----
            val start = draft.copy(
                word = MoodWord.Preset(Mood.Tense),
                ownWordText = "  ",
                ownWordOpen = true,
            )

            // ----- Act -----
            val result = start.withOwnWordEvent(TextFieldEvent.Submitted(key = "own-word"))

            // ----- Assert -----
            result.word shouldBe MoodWord.Preset(Mood.Tense)
            result.ownWordOpen shouldBe false
        }

        @Test
        fun `a trailing click commits the typed own word and closes the input`() {
            // ----- Arrange -----
            val start = draft.copy(
                word = MoodWord.Preset(Mood.Tense),
                ownWordText = "Giddy",
                ownWordOpen = true,
            )

            // ----- Act -----
            val result = start.withOwnWordEvent(TextFieldEvent.TrailingClicked(key = "own-word"))

            // ----- Assert -----
            result.word shouldBe MoodWord.Own("Giddy")
            result.ownWordText shouldBe ""
            result.ownWordOpen shouldBe false
        }

        @Test
        fun `shake end changes nothing`() {
            // ----- Arrange -----
            val start = draft.copy(ownWordText = "Giddy")

            // ----- Act -----
            val shaken = start.withOwnWordEvent(TextFieldEvent.ShakeEnded("own-word"))

            // ----- Assert -----
            shaken shouldBe start
        }
    }
}
