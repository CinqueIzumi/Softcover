package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.uibinding.mood.OWN_WORD_CHIP_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.state.HowItFeltDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowItFeltSheetMapperTest {
    @Nested
    inner class ToHowItFeltSheet {
        @Test
        fun `lists the presets in order, then own words in use, then the own word chip`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft()

            // ----- Act -----
            val result = draft.toHowItFeltSheet(
                bookTitle = "Dune",
                ownWords = listOf("Wistful", "Eerie"),
            )

            // ----- Assert -----
            result.choices.chips.chips.map { it.label } shouldBe listOf(
                "Tense",
                "Hopeful",
                "Funny",
                "Sad",
                "Reflective",
                "Dark",
                "Cosy",
                "Melancholy",
                "Mysterious",
                "Wistful",
                "Eerie",
                "Your own word",
            )

            result.choices.chips.chips.last().key shouldBe OWN_WORD_CHIP_KEY
        }

        @Test
        fun `marks the chosen moods selected and adds a chosen own word not yet in use`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(
                moods = persistentListOf(
                    BookMood(word = MoodWord.Preset(Mood.Sad)),
                    BookMood(word = MoodWord.Own("Fresh")),
                ),
            )

            // ----- Act -----
            val result = draft.toHowItFeltSheet(
                bookTitle = "Dune",
                ownWords = listOf("Wistful"),
            )

            // ----- Assert -----
            result.choices.chips.chips.filter { it.selected }.map { it.label } shouldBe listOf("Sad", "Fresh")
            result.choices.chips.chips.map { it.label }.takeLast(3) shouldBe listOf("Wistful", "Fresh", "Your own word")
        }

        @Test
        fun `lists a strength row per chosen mood, in the order chosen, with its strength`() {
            // ----- Arrange -----
            val draft = HowItFeltDraft(
                moods = persistentListOf(
                    BookMood(
                        word = MoodWord.Preset(Mood.Dark),
                        strength = 75,
                    ),
                    BookMood(word = MoodWord.Own("Wistful")),
                ),
            )

            // ----- Act -----
            val result = draft.toHowItFeltSheet(
                bookTitle = "Dune",
                ownWords = emptyList(),
            )

            // ----- Assert -----
            result.strengths.map {
                Triple(
                    it.key,
                    it.label,
                    it.strength,
                )
            } shouldBe listOf(
                Triple(
                    "dark",
                    "Dark",
                    75,
                ),
                Triple(
                    "own:Wistful",
                    "Wistful",
                    null,
                ),
            )
        }
    }
}
