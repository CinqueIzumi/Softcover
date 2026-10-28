package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.profile.domain.model.ReadingActivity
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDay
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDayBook
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStreak
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasStreaks
import nl.rhaydus.softcover.feature.profile.presentation.state.DayDetailUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DayDetailMapperTest {
    private val twoBooksWithTimes: DayDetailUiModel
        get() = DayDetailUiModel.previews.getValue("Two books, with update times")

    private val finishedRemainderNoTimes: DayDetailUiModel
        get() = DayDetailUiModel.previews.getValue("Finished remainder, missing book")

    private val date = LocalDate(
        year = 2026,
        month = 4,
        day = 14,
    )

    private fun book(id: Int): Book = Book(
        id = id,
        title = "Book $id",
        editions = emptyList(),
        defaultEdition = null,
        rating = 4.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
    )

    private fun dayBook(
        bookId: Int,
        pages: Int,
    ): ReadingDayBook = ReadingDayBook(
        bookId = bookId,
        fromPage = 1,
        toPage = pages,
        updateCount = 1,
        pages = pages,
        finished = false,
        firstUpdateAt = null,
        lastUpdateAt = null,
    )

    private fun clock(
        hour: Int,
        minute: Int,
        second: Int = 0,
    ) = LocalTime(
        hour = hour,
        minute = minute,
        second = second,
    )

    private fun captionOf(book: ReadingDayBook): String {
        val day = ReadingDay(
            date = date,
            books = listOf(book),
        )
        val model = dayDetailUiModel(
            day = day,
            books = libraryOf(book.bookId),
            currentYear = 2026,
            streaks = AtlasStreaks(),
        )

        return model.captionAt(0)
    }

    private fun libraryOf(vararg ids: Int): List<Book> = ids.map { book(it) }

    private val singlePageDay = listOf(
        dayBook(
            bookId = 1,
            pages = 1,
        ),
    )

    private val unsortedDay = listOf(
        dayBook(
            bookId = 3,
            pages = 30,
        ),
        dayBook(
            bookId = 1,
            pages = 20,
        ),
        dayBook(
            bookId = 2,
            pages = 10,
        ),
    )

    private fun DayDetailUiModel.captionAt(index: Int): String {
        val row = books.cards[index].variant.shouldBeInstanceOf<BookCardVariant.Row>()
        val part = row.text.supportingLines.single().single()

        return part.shouldBeInstanceOf<BookCardLinePart.Text>().text
    }

    private fun DayDetailUiModel.badgeAt(index: Int): String {
        val row = books.cards[index].variant.shouldBeInstanceOf<BookCardVariant.Row>()

        return row.trailing.shouldBeInstanceOf<BookCardTrailing.Badge>().model.label
    }

    private fun DayDetailUiModel.titleAt(index: Int): String {
        val row = books.cards[index].variant.shouldBeInstanceOf<BookCardVariant.Row>()

        return row.text.title
    }

    private fun BookCardUiModel.bookId(): Int = key.shouldBeInstanceOf<BookCardKey.Book>().bookId

    private fun streakDescriptionOn(
        day: LocalDate,
        streaks: AtlasStreaks,
    ): String? {
        val reading = ReadingDay(
            date = day,
            books = singlePageDay,
        )

        return dayDetailUiModel(
            day = reading,
            books = libraryOf(1),
            currentYear = 2026,
            streaks = streaks,
        ).scaffold.header.shouldNotBeNull().section.description?.joinToString(separator = "") { it.text }
    }

    private fun run(
        startDay: Int,
        endDay: Int,
    ): ReadingStreak = ReadingStreak(
        start = LocalDate(
            year = 2026,
            month = 4,
            day = startDay,
        ),
        end = LocalDate(
            year = 2026,
            month = 4,
            day = endDay,
        ),
    )

    private fun streaksOf(
        vararg runs: ReadingStreak,
        longest: ReadingStreak? = null,
        current: ReadingStreak? = null,
    ): AtlasStreaks = AtlasStreaks(
        activity = ReadingActivity(
            pagesByDay = emptyMap(),
            streaks = runs.toList(),
        ),
        longestInAtlasYear = longest,
        current = current,
    )

    @Nested
    inner class Eyebrow {
        @Test
        fun `omits the year when the day is in the current year`() {
            // ----- Arrange -----
            val model = twoBooksWithTimes

            // ----- Act -----
            val eyebrow = model.scaffold.header.shouldNotBeNull().section.eyebrow

            // ----- Assert -----
            eyebrow shouldBe "Tuesday 14 April"
        }

        @Test
        fun `appends the year when the day is in another year`() {
            // ----- Arrange -----
            val model = finishedRemainderNoTimes

            // ----- Act -----
            val eyebrow = model.scaffold.header.shouldNotBeNull().section.eyebrow

            // ----- Assert -----
            eyebrow shouldBe "Tuesday 14 April 2026"
        }
    }

    @Nested
    inner class StreakDescription {
        @Test
        fun `names the position in the longest streak of the atlas year`() {
            // ----- Arrange -----
            val longest = run(
                startDay = 10,
                endDay = 20,
            )
            val streaks = streaksOf(
                longest,
                longest = longest,
            )

            // ----- Act -----
            val result = streakDescriptionOn(
                day = LocalDate(
                    year = 2026,
                    month = 4,
                    day = 14,
                ),
                streaks = streaks,
            )

            // ----- Assert -----
            result shouldBe "Day 5 of your longest streak."
        }

        @Test
        fun `names the position in the current streak when it is not the longest`() {
            // ----- Arrange -----
            val current = run(
                startDay = 12,
                endDay = 15,
            )
            val streaks = streaksOf(
                current,
                current = current,
            )

            // ----- Act -----
            val result = streakDescriptionOn(
                day = LocalDate(
                    year = 2026,
                    month = 4,
                    day = 14,
                ),
                streaks = streaks,
            )

            // ----- Assert -----
            result shouldBe "Day 3 of your current streak."
        }

        @Test
        fun `prefers longest when the run is both the longest and the current`() {
            // ----- Arrange -----
            val both = run(
                startDay = 14,
                endDay = 18,
            )
            val streaks = streaksOf(
                both,
                longest = both,
                current = both,
            )

            // ----- Act -----
            val result = streakDescriptionOn(
                day = LocalDate(
                    year = 2026,
                    month = 4,
                    day = 14,
                ),
                streaks = streaks,
            )

            // ----- Assert -----
            result shouldBe "Day 1 of your longest streak."
        }

        @Test
        fun `gives the length of any other streak of two days or more`() {
            // ----- Arrange -----
            val other = run(
                startDay = 13,
                endDay = 16,
            )
            val streaks = streaksOf(other)

            // ----- Act -----
            val result = streakDescriptionOn(
                day = LocalDate(
                    year = 2026,
                    month = 4,
                    day = 16,
                ),
                streaks = streaks,
            )

            // ----- Assert -----
            result shouldBe "Day 4 of a 4-day streak."
        }

        @Test
        fun `says nothing for a one-day run`() {
            // ----- Arrange -----
            val single = run(
                startDay = 14,
                endDay = 14,
            )
            val streaks = streaksOf(single)

            // ----- Act -----
            val result = streakDescriptionOn(
                day = LocalDate(
                    year = 2026,
                    month = 4,
                    day = 14,
                ),
                streaks = streaks,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `says nothing for a one-day run that is the longest of the year`() {
            // ----- Arrange -----
            val single = run(
                startDay = 14,
                endDay = 14,
            )
            val streaks = streaksOf(
                single,
                longest = single,
            )

            // ----- Act -----
            val result = streakDescriptionOn(
                day = date,
                streaks = streaks,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `says nothing for a one-day run that is the current streak`() {
            // ----- Arrange -----
            val single = run(
                startDay = 14,
                endDay = 14,
            )
            val streaks = streaksOf(
                single,
                current = single,
            )

            // ----- Act -----
            val result = streakDescriptionOn(
                day = date,
                streaks = streaks,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `says nothing when the day belongs to no run`() {
            // ----- Arrange -----
            val streaks = AtlasStreaks()

            // ----- Act -----
            val result = streakDescriptionOn(
                day = date,
                streaks = streaks,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class Headline {
        @Test
        fun `pluralises the totals of several pages and books`() {
            // ----- Arrange -----
            val model = twoBooksWithTimes

            // ----- Act -----
            val headline = model.scaffold.header.shouldNotBeNull().section.headlineText

            // ----- Assert -----
            headline shouldBe "62 pages across 2 books"
        }

        @Test
        fun `uses the singular for one page in one book`() {
            // ----- Arrange -----
            val day = ReadingDay(
                date = date,
                books = singlePageDay,
            )

            // ----- Act -----
            val model = dayDetailUiModel(
                day = day,
                books = libraryOf(1),
                currentYear = 2026,
                streaks = AtlasStreaks(),
            )

            // ----- Assert -----
            model.scaffold.header.shouldNotBeNull().section.headlineText shouldBe "1 page across 1 book"
        }
    }

    @Nested
    inner class Captions {
        @Test
        fun `shows the page range with the first to last update times of several updates`() {
            // ----- Arrange -----
            val model = twoBooksWithTimes

            // ----- Act -----
            val caption = model.captionAt(0)

            // ----- Assert -----
            caption shouldBe "Pages 112 to 158 · 2 updates, 09:10 to 22:40"
            model.badgeAt(0) shouldBe "46 pages"
        }

        @Test
        fun `reads up to the last page and a single update at its time when the day starts at page 0`() {
            // ----- Arrange -----
            val model = twoBooksWithTimes

            // ----- Act -----
            val caption = model.captionAt(1)

            // ----- Assert -----
            caption shouldBe "Up to page 16 · 1 update at 08:05"
        }

        @Test
        fun `drops the update part and marks a finished remainder`() {
            // ----- Arrange -----
            val model = finishedRemainderNoTimes

            // ----- Act -----
            val caption = model.captionAt(0)

            // ----- Assert -----
            caption shouldBe "Pages 380 to 412 · finished"
        }

        @Test
        fun `uses a single time when several updates share one minute`() {
            // ----- Arrange -----
            val book = ReadingDayBook(
                bookId = 1,
                fromPage = 10,
                toPage = 30,
                updateCount = 2,
                pages = 20,
                finished = false,
                firstUpdateAt = clock(
                    hour = 14,
                    minute = 41,
                    second = 5,
                ),
                lastUpdateAt = clock(
                    hour = 14,
                    minute = 41,
                    second = 50,
                ),
            )

            // ----- Act -----
            val caption = captionOf(book)

            // ----- Assert -----
            caption shouldBe "Pages 10 to 30 · 2 updates at 14:41"
        }

        @Test
        fun `adds finished after the update times`() {
            // ----- Arrange -----
            val book = ReadingDayBook(
                bookId = 1,
                fromPage = 380,
                toPage = 412,
                updateCount = 2,
                pages = 32,
                finished = true,
                firstUpdateAt = clock(
                    hour = 9,
                    minute = 10,
                ),
                lastUpdateAt = clock(
                    hour = 22,
                    minute = 40,
                ),
            )

            // ----- Act -----
            val caption = captionOf(book)

            // ----- Assert -----
            caption shouldBe "Pages 380 to 412 · 2 updates, 09:10 to 22:40 · finished"
        }

        @Test
        fun `keeps the plural count without times when a book has none recorded`() {
            // ----- Arrange -----
            val book = ReadingDayBook(
                bookId = 1,
                fromPage = 0,
                toPage = 40,
                updateCount = 3,
                pages = 40,
                finished = false,
                firstUpdateAt = null,
                lastUpdateAt = null,
            )

            // ----- Act -----
            val caption = captionOf(book)

            // ----- Assert -----
            caption shouldBe "Up to page 40 · 3 updates"
        }
    }

    @Nested
    inner class MissingBook {
        @Test
        fun `shows an untappable placeholder row with its pages`() {
            // ----- Arrange -----
            val model = finishedRemainderNoTimes

            // ----- Act -----
            val row = model.books.cards[1]

            // ----- Assert -----
            model.titleAt(1) shouldBe "A book no longer in your library"
            model.badgeAt(1) shouldBe "20 pages"
            row.decorations.pressBehaviour shouldBe BookCardDecorations.Press.None
            row.variant.shouldBeInstanceOf<BookCardVariant.Row>().lead.shouldBeNull()
            model.books[row.key].shouldNotBeNull().initialCover.shouldBeNull()
        }

        @Test
        fun `keeps a book found in the library tappable with its title`() {
            // ----- Arrange -----
            val model = finishedRemainderNoTimes

            // ----- Act -----
            val row = model.books.cards[0]

            // ----- Assert -----
            model.titleAt(0) shouldBe "Piranesi"
            row.decorations.pressBehaviour shouldBe BookCardDecorations.Press.Tap
            model.books[row.key].shouldNotBeNull().initialCover.shouldNotBeNull()
        }
    }

    @Nested
    inner class Order {
        @Test
        fun `rows follow the order of the reading day`() {
            // ----- Arrange -----
            val day = ReadingDay(
                date = date,
                books = unsortedDay,
            )

            // ----- Act -----
            val model = dayDetailUiModel(
                day = day,
                books = libraryOf(
                    1,
                    2,
                    3,
                ),
                currentYear = 2026,
                streaks = AtlasStreaks(),
            )

            // ----- Assert -----
            model.books.cards.map { it.bookId() } shouldContainExactly listOf(3, 1, 2)
        }
    }
}
