package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.field.TextFieldVariant
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class MoodMomentSheetMapperTest {
    private val adding = MoodMomentDraft.adding(
        PendingMoodMoment(
            bookId = 7,
            page = 286,
            seconds = null,
            percent = 62,
        ),
    )

    private val editing = MoodMomentDraft.editing(
        MoodMoment(
            id = 3,
            bookId = 7,
            word = MoodWord.Preset(Mood.Tense),
            strength = 70,
            percent = 62,
            page = 286,
            seconds = null,
            recordedAt = 1,
        ),
    )

    @Nested
    inner class Choices {
        @Test
        fun `lists the moment presets, then own words, then the own-word chip`() {
            // ----- Arrange -----
            val ownWords = listOf("Giddy", "Wistful")

            // ----- Act -----
            val model = adding.toMoodMomentSheet(ownWords = ownWords)

            // ----- Assert -----
            model.choices.chips.chips.map { it.label } shouldBe listOf(
                "Curious",
                "Tense",
                "Wrecked",
                "Hopeful",
                "Bored",
                "Gripped",
                "Giddy",
                "Wistful",
                "Your own word",
            )
        }

        @Test
        fun `marks only the chosen word as selected`() {
            // ----- Arrange -----
            val draft = adding.copy(word = MoodWord.Preset(Mood.Wrecked))

            // ----- Act -----
            val model = draft.toMoodMomentSheet(ownWords = emptyList())

            // ----- Assert -----
            model.choices.chips.chips.filter { it.selected }.map { it.label } shouldBe listOf("Wrecked")
        }

        @Test
        fun `adds a chosen own word that is not yet in use as a chip`() {
            // ----- Arrange -----
            val draft = adding.copy(word = MoodWord.Own("Giddy"))

            // ----- Act -----
            val model = draft.toMoodMomentSheet(ownWords = emptyList())

            // ----- Assert -----
            model.choices.chips.chips.filter { it.selected }.map { it.label } shouldBe listOf("Giddy")
        }

        @Test
        fun `shows the own-word add pill only once text is typed`() {
            // ----- Arrange -----
            val blank = adding.copy(ownWordText = "  ")
            val typed = adding.copy(ownWordText = "Giddy")

            // ----- Act -----
            val blankVariant = blank.toMoodMomentSheet(ownWords = emptyList()).choices.ownWord.variant
            val typedVariant = typed.toMoodMomentSheet(ownWords = emptyList()).choices.ownWord.variant

            // ----- Assert -----
            (blankVariant as TextFieldVariant.Pill).trailing.shouldBeNull()
            (typedVariant as TextFieldVariant.Pill).trailing.shouldNotBeNull()
        }
    }

    @Nested
    inner class Scaffold {
        @Test
        fun `adding offers Add mood and Skip`() {
            // ----- Arrange -----
            val draft = adding

            // ----- Act -----
            val footer = draft.toMoodMomentSheet(ownWords = emptyList()).scaffold.footer

            // ----- Assert -----
            val inline = footer as SheetFooterUiModel.Inline
            inline.primary.label shouldBe "Add mood"
            inline.secondary.label shouldBe "Skip"
        }

        @Test
        fun `editing offers Save and Delete`() {
            // ----- Arrange -----
            val draft = editing

            // ----- Act -----
            val footer = draft.toMoodMomentSheet(ownWords = emptyList()).scaffold.footer

            // ----- Assert -----
            val inline = footer as SheetFooterUiModel.Inline
            inline.primary.label shouldBe "Save"
            inline.secondary.label shouldBe "Delete"
        }

        @Test
        fun `the eyebrow shows the moment's position`() {
            // ----- Arrange -----
            val draft = adding

            // ----- Act -----
            val section = draft.toMoodMomentSheet(ownWords = emptyList()).scaffold.header?.section

            // ----- Assert -----
            section?.eyebrow shouldBe "Page 286 · 62%"
        }

        @Test
        fun `only adding says progress was saved`() {
            // ----- Arrange -----
            val addingDraft = adding
            val editingDraft = editing

            // ----- Act -----
            val addingText = addingDraft.toMoodMomentSheet(ownWords = emptyList()).scaffold.header?.section?.description
                ?.joinToString("") { it.text }

            val editingText = editingDraft.toMoodMomentSheet(ownWords = emptyList()).scaffold.header?.section?.description
                ?.joinToString("") { it.text }

            // ----- Assert -----
            addingText?.startsWith("Progress saved.") shouldBe true
            editingText?.startsWith("Progress saved.") shouldBe false
        }
    }
}
