package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodMoment
import nl.rhaydus.softcover.core.domain.model.MoodWord
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowItFeltBlockMapperTest {
    private fun moment(
        id: Long,
        mood: Mood = Mood.Curious,
        percent: Int? = null,
        page: Int? = null,
        seconds: Int? = null,
        strength: Int? = null,
        recordedAt: Long = id,
    ): MoodMoment = MoodMoment(
        id = id,
        bookId = 1,
        word = MoodWord.Preset(mood),
        strength = strength,
        percent = percent,
        page = page,
        seconds = seconds,
        recordedAt = recordedAt,
    )

    @Nested
    inner class ToHowItFeltBlock {
        @Test
        fun `lists preset moods in the sheet order, then own words`() {
            // ----- Arrange -----
            val moods = listOf(
                BookMood(word = MoodWord.Own("Wistful")),
                BookMood(word = MoodWord.Preset(Mood.Mysterious)),
                BookMood(word = MoodWord.Preset(Mood.Tense)),
                BookMood(word = MoodWord.Preset(Mood.Sad)),
            )

            // ----- Act -----
            val result = toHowItFeltBlock(
                moods = moods,
                moments = emptyList(),
            )

            // ----- Assert -----
            result.moods.map { it.label } shouldBe listOf("Tense", "Sad", "Mysterious", "Wistful")
            result.moods.map { it.key } shouldBe listOf("tense", "sad", "mysterious", "own:Wistful")
        }

        @Test
        fun `with no moods and no moments the block is empty`() {
            // ----- Arrange -----
            val moods = emptyList<BookMood>()
            val moments = emptyList<MoodMoment>()

            // ----- Act -----
            val result = toHowItFeltBlock(
                moods = moods,
                moments = moments,
            )

            // ----- Assert -----
            result.isEmpty shouldBe true
            result.moods.shouldBeEmpty()
            result.moments.shouldBeEmpty()
        }

        @Test
        fun `a moment alone makes the block non-empty`() {
            // ----- Arrange -----
            val moments = listOf(
                moment(
                    id = 1,
                    percent = 10,
                ),
            )

            // ----- Act -----
            val result = toHowItFeltBlock(
                moods = emptyList(),
                moments = moments,
            )

            // ----- Assert -----
            result.isEmpty shouldBe false
        }

        @Test
        fun `sorts moments by position across reads, unplaced last, ties by recording time`() {
            // ----- Arrange -----
            val moments = listOf(
                moment(
                    id = 1,
                    page = 30,
                ),
                moment(
                    id = 2,
                    percent = 62,
                    recordedAt = 500,
                ),
                moment(
                    id = 3,
                    percent = 18,
                ),
                moment(
                    id = 4,
                    percent = 62,
                    recordedAt = 100,
                ),
            )

            // ----- Act -----
            val result = toHowItFeltBlock(
                moods = emptyList(),
                moments = moments,
            )

            // ----- Assert -----
            result.moments.map { it.id } shouldBe listOf("3", "4", "2", "1")
        }

        @Test
        fun `a moment's position reads as percent, then page, then clock time`() {
            // ----- Arrange -----
            val moments = listOf(
                moment(
                    id = 1,
                    percent = 40,
                    page = 120,
                ),
                moment(
                    id = 2,
                    page = 120,
                ),
                moment(
                    id = 3,
                    seconds = 3_723,
                ),
            )

            // ----- Act -----
            val result = toHowItFeltBlock(
                moods = emptyList(),
                moments = moments,
            )

            // ----- Assert -----
            result.moments.map { it.at } shouldBe listOf("40%", "p. 120", "1:02:03")
        }
    }

    @Nested
    inner class ToMoodRibbon {
        @Test
        fun `is null for no moments`() {
            // ----- Arrange -----
            val moments = emptyList<MoodMoment>()

            // ----- Act -----
            val result = moments.toMoodRibbon()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `is null when no moment has a percent`() {
            // ----- Arrange -----
            val moments = listOf(
                moment(
                    id = 1,
                    page = 30,
                ),
            )

            // ----- Act -----
            val result = moments.toMoodRibbon()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `places marks as fractions sorted by position and skips unplaced moments`() {
            // ----- Arrange -----
            val moments = listOf(
                moment(
                    id = 1,
                    mood = Mood.Wrecked,
                    percent = 62,
                    strength = 90,
                ),
                moment(
                    id = 2,
                    page = 5,
                ),
                moment(
                    id = 3,
                    mood = Mood.Curious,
                    percent = 18,
                ),
            )

            // ----- Act -----
            val result = moments.toMoodRibbon().shouldNotBeNull()

            // ----- Assert -----
            result.marks.map { it.word } shouldBe listOf("curious", "wrecked")
            result.marks.map { it.position } shouldBe listOf(0.18f, 0.62f)
            result.marks.map { it.strength } shouldBe listOf(null, 90)
        }

        @Test
        fun `describes the edges as the start and the end`() {
            // ----- Arrange -----
            val moments = listOf(
                moment(
                    id = 1,
                    mood = Mood.Hopeful,
                    percent = 0,
                ),
                moment(
                    id = 2,
                    mood = Mood.Tense,
                    percent = 40,
                ),
                moment(
                    id = 3,
                    mood = Mood.Sad,
                    percent = 100,
                ),
            )

            // ----- Act -----
            val result = moments.toMoodRibbon().shouldNotBeNull()

            // ----- Assert -----
            result.contentDescription shouldBe
                "Moods along the way: hopeful at the start, tense at 40%, sad at the end"
        }
    }
}
