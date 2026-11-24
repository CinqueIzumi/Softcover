package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookMood
import nl.rhaydus.softcover.core.domain.model.BookTraits
import nl.rhaydus.softcover.core.domain.model.Mood
import nl.rhaydus.softcover.core.domain.model.MoodWord
import nl.rhaydus.softcover.core.domain.model.Pace
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.domain.model.WritingFlag
import nl.rhaydus.softcover.core.domain.model.WritingStyle
import nl.rhaydus.softcover.core.profile.domain.model.MoodScore
import nl.rhaydus.softcover.core.profile.domain.model.PlotCharacterSplit
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HowYouReadStatsMapperTest {
    private val bounded = StatsRange.Bounded(
        start = LocalDate(
            2026,
            1,
            1,
        ),
        endInclusive = LocalDate(
            2026,
            12,
            31,
        ),
    )

    private fun inRange(id: Int): Book = readBook(
        id = id,
        finishedAt = "2026-03-01",
    )

    private fun List<Book>.toStats(
        traitsByBookId: Map<Int, BookTraits> = emptyMap(),
        moodsByBookId: Map<Int, List<BookMood>> = emptyMap(),
    ) = toHowYouReadStats(
        range = bounded,
        updates = emptyList(),
        traitsByBookId = traitsByBookId,
        moodsByBookId = moodsByBookId,
        timeZone = TimeZone.UTC,
    )

    private fun own(
        text: String,
        strength: Int? = null,
    ) = BookMood(
        word = MoodWord.Own(text),
        strength = strength,
    )

    private fun balanceOf(balances: List<Int?>): PlotCharacterSplit {
        val books = balances.indices.map { inRange(it + 1) }

        val traits = balances.withIndex().associate { (index, balance) ->
            index + 1 to BookTraits(
                bookId = index + 1,
                plotCharacterBalance = balance,
            )
        }

        return books.toStats(traitsByBookId = traits).plotCharacter
    }

    @Nested
    inner class PlotCharacter {
        @Test
        fun `buckets the balance into plot, mix and character at the boundaries`() {
            // ----- Arrange -----
            val balances = listOf(40, 41, 59, 60)

            // ----- Act -----
            val split = balanceOf(balances)

            // ----- Assert -----
            split shouldBe PlotCharacterSplit(
                plot = 1,
                mix = 2,
                character = 1,
            )
        }

        @Test
        fun `does not count an unanswered balance`() {
            // ----- Arrange -----
            val balances = listOf(null, 0, 100)

            // ----- Act -----
            val split = balanceOf(balances)

            // ----- Assert -----
            split shouldBe PlotCharacterSplit(
                plot = 1,
                mix = 0,
                character = 1,
            )
        }
    }

    @Nested
    inner class TraitCounts {
        @Test
        fun `counts each answer and omits answers nobody gave`() {
            // ----- Arrange -----
            val books = listOf(inRange(1), inRange(2), inRange(3))

            val traits = mapOf(
                1 to BookTraits(
                    bookId = 1,
                    pace = Pace.FAST,
                    writingStyle = WritingStyle.SIMPLE,
                ),
                2 to BookTraits(
                    bookId = 2,
                    pace = Pace.FAST,
                    writingStyle = WritingStyle.DEMANDING,
                ),
                3 to BookTraits(bookId = 3),
            )

            // ----- Act -----
            val stats = books.toStats(traitsByBookId = traits)

            // ----- Assert -----
            stats.paceCounts shouldBe mapOf(Pace.FAST to 2)

            stats.writingStyleCounts shouldBe mapOf(
                WritingStyle.SIMPLE to 1,
                WritingStyle.DEMANDING to 1,
            )

            stats.booksWithPace shouldBe 2
            stats.booksWithWritingStyle shouldBe 2
        }

        @Test
        fun `counts a book with two flags once in the total but in each flag`() {
            // ----- Arrange -----
            val books = listOf(inRange(1), inRange(2))

            val traits = mapOf(
                1 to BookTraits(
                    bookId = 1,
                    writingFlags = setOf(WritingFlag.VIVID, WritingFlag.POETIC),
                ),
                2 to BookTraits(
                    bookId = 2,
                    writingFlags = setOf(WritingFlag.VIVID),
                ),
            )

            // ----- Act -----
            val stats = books.toStats(traitsByBookId = traits)

            // ----- Assert -----
            stats.writingFlagCounts shouldBe mapOf(
                WritingFlag.VIVID to 2,
                WritingFlag.POETIC to 1,
            )

            stats.booksWithWritingFlag shouldBe 2
        }

        @Test
        fun `ignores a book outside the range even when it has answers`() {
            // ----- Arrange -----
            val books = listOf(
                inRange(1),
                readBook(
                    id = 2,
                    finishedAt = "2025-03-01",
                ),
            )

            val traits = mapOf(
                1 to BookTraits(
                    bookId = 1,
                    pace = Pace.SLOW,
                    plotCharacterBalance = 10,
                ),
                2 to BookTraits(
                    bookId = 2,
                    pace = Pace.SLOW,
                    plotCharacterBalance = 10,
                ),
            )

            // ----- Act -----
            val stats = books.toStats(traitsByBookId = traits)

            // ----- Assert -----
            stats.paceCounts shouldBe mapOf(Pace.SLOW to 1)
            stats.plotCharacter.plot shouldBe 1
        }
    }

    @Nested
    inner class Moods {
        @Test
        fun `folds an own word matching a preset name into the preset ignoring case and whitespace`() {
            // ----- Arrange -----
            val books = listOf(inRange(1), inRange(2))

            val moods = mapOf(
                1 to listOf(
                    BookMood(
                        word = MoodWord.Preset(Mood.Tense),
                        strength = 2,
                    ),
                ),
                2 to listOf(
                    own(
                        text = "  TENSE ",
                        strength = 4,
                    ),
                ),
            )

            // ----- Act -----
            val profile = books.toStats(moodsByBookId = moods).moods

            // ----- Assert -----
            profile.topMoods shouldBe listOf(
                MoodScore(
                    word = MoodWord.Preset(Mood.Tense),
                    bookCount = 2,
                    averageStrength = 3.0,
                ),
            )
        }

        @Test
        fun `merges own words ignoring case and shows the most used spelling`() {
            // ----- Arrange -----
            val books = listOf(inRange(1), inRange(2), inRange(3))

            val moods = mapOf(
                1 to listOf(own("bright")),
                2 to listOf(own("bright")),
                3 to listOf(own("Bright")),
            )

            // ----- Act -----
            val profile = books.toStats(moodsByBookId = moods).moods

            // ----- Assert -----
            profile.topMoods.map { it.word to it.bookCount } shouldBe listOf(MoodWord.Own("bright") to 3)
        }

        @Test
        fun `shows the alphabetically first spelling when spellings tie`() {
            // ----- Arrange -----
            val books = listOf(inRange(1), inRange(2))

            val moods = mapOf(
                1 to listOf(own("bright")),
                2 to listOf(own("Bright")),
            )

            // ----- Act -----
            val profile = books.toStats(moodsByBookId = moods).moods

            // ----- Assert -----
            profile.topMoods.map { it.word } shouldBe listOf(MoodWord.Own("Bright"))
        }

        @Test
        fun `counts a word once per book`() {
            // ----- Arrange -----
            val books = listOf(inRange(1))

            val moods = mapOf(
                1 to listOf(
                    own(
                        "calm",
                        2,
                    ),
                    own(
                        "Calm",
                        4,
                    ),
                ),
            )

            // ----- Act -----
            val profile = books.toStats(moodsByBookId = moods).moods

            // ----- Assert -----
            profile.topMoods.single().bookCount shouldBe 1
        }

        @Test
        fun `averages one strength per book when a book repeats a word`() {
            // ----- Arrange -----
            val books = listOf(inRange(1), inRange(2))

            val moods = mapOf(
                1 to listOf(
                    own(
                        "calm",
                        2,
                    ),
                    own(
                        "Calm",
                        4,
                    ),
                ),
                2 to listOf(
                    own(
                        "calm",
                        5,
                    ),
                ),
            )

            // ----- Act -----
            val score = books.toStats(moodsByBookId = moods).moods.topMoods.single()

            // ----- Assert -----
            score.bookCount shouldBe 2
            score.averageStrength shouldBe 4.0
        }

        @Test
        fun `treats strength zero as set and leaves the average null when none is set`() {
            // ----- Arrange -----
            val books = listOf(inRange(1))

            val moods = mapOf(
                1 to listOf(
                    own(
                        "zero",
                        0,
                    ),
                    own(
                        "unset",
                        null,
                    ),
                ),
            )

            // ----- Act -----
            val scores = books.toStats(moodsByBookId = moods).moods.topMoods.associate { it.word to it.averageStrength }

            // ----- Assert -----
            scores shouldBe mapOf(
                MoodWord.Own("zero") to 0.0,
                MoodWord.Own("unset") to null,
            )
        }

        @Test
        fun `counts only in-range books with moods in booksWithMoods`() {
            // ----- Arrange -----
            val books = listOf(
                inRange(1),
                inRange(2),
                inRange(3),
                readBook(
                    id = 4,
                    finishedAt = "2025-03-01",
                ),
            )

            val moods = mapOf(
                1 to listOf(own("calm")),
                2 to emptyList(),
                4 to listOf(own("calm")),
            )

            // ----- Act -----
            val profile = books.toStats(moodsByBookId = moods).moods

            // ----- Assert -----
            profile.booksWithMoods shouldBe 1
            profile.topMoods.single().bookCount shouldBe 1
        }
    }

    @Nested
    inner class TopFive {
        @Test
        fun `selects by most books then displays by average strength with null last`() {
            // ----- Arrange -----
            val books = listOf(inRange(1), inRange(2))

            val moods = mapOf(
                1 to listOf(
                    own("u"),
                    own(
                        "p",
                        5,
                    ),
                    own(
                        "q",
                        4,
                    ),
                    own(
                        "r",
                        3,
                    ),
                    own(
                        "s",
                        2,
                    ),
                ),
                2 to listOf(own("u")),
            )

            // ----- Act -----
            val words = books.toStats(moodsByBookId = moods).moods.topMoods.map { it.word }

            // ----- Assert -----
            words shouldBe listOf("p", "q", "r", "s", "u").map { MoodWord.Own(it) }
        }

        @Test
        fun `breaks a count tie by higher average then label and cuts at five`() {
            // ----- Arrange -----
            val books = listOf(inRange(1))

            val moods = mapOf(
                1 to listOf(
                    own(
                        "f",
                        3,
                    ),
                    own(
                        "b",
                        3,
                    ),
                    own(
                        "a",
                        3,
                    ),
                    own(
                        "e",
                        5,
                    ),
                    own(
                        "d",
                        5,
                    ),
                    own(
                        "c",
                        5,
                    ),
                ),
            )

            // ----- Act -----
            val words = books.toStats(moodsByBookId = moods).moods.topMoods.map { it.word }

            // ----- Assert -----
            words shouldBe listOf("c", "d", "e", "a", "b").map { MoodWord.Own(it) }
        }

        @Test
        fun `ranks a null average below strength zero when cutting`() {
            // ----- Arrange -----
            val books = listOf(inRange(1))

            val moods = mapOf(
                1 to listOf(
                    own(
                        "a",
                        0,
                    ),
                    own(
                        "b",
                        0,
                    ),
                    own(
                        "c",
                        0,
                    ),
                    own(
                        "d",
                        0,
                    ),
                    own(
                        "e",
                        0,
                    ),
                    own(
                        "a-null",
                        null,
                    ),
                ),
            )

            // ----- Act -----
            val words = books.toStats(moodsByBookId = moods).moods.topMoods.map { it.word }

            // ----- Assert -----
            words shouldBe listOf("a", "b", "c", "d", "e").map { MoodWord.Own(it) }
        }
    }
}
