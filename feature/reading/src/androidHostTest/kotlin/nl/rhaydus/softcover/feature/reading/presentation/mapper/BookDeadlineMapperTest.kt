package nl.rhaydus.softcover.feature.reading.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock

class BookDeadlineMapperTest {
    private val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

    private fun deadline(unit: DeadlineUnit): BookDeadline = BookDeadline(
        bookId = 1,
        deadlineDate = today.plus(DatePeriod(days = 10)),
        setAt = today,
        initialPerDay = 10f,
        unit = unit,
    )

    private fun book(
        hasEdition: Boolean = true,
        pages: Int? = 100,
        audioSeconds: Int? = 36000,
        read: UserBookRead? = null,
    ): Book = Book(
        id = 1,
        title = "Title",
        editions = emptyList(),
        defaultEdition = if (hasEdition) {
            BookEdition(
                id = 50,
                canonicalId = null,
                bookId = 1,
                publisher = null,
                title = null,
                url = null,
                localImagePath = null,
                isbn10 = null,
                isbn13 = null,
                pages = pages,
                audioSeconds = audioSeconds,
                authors = emptyList(),
                releaseYear = 2020,
                format = "Paperback",
                owned = false,
            )
        } else {
            null
        },
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = PreviewData.baseBook.userBook?.copy(
            status = BookStatus.Reading,
            reads = listOfNotNull(read),
        ),
    )

    private fun read(
        page: Int? = null,
        seconds: Int? = null,
    ): UserBookRead = UserBookRead(
        id = 1,
        currentPage = page,
        currentSeconds = seconds,
        progress = 0f,
        startedAt = null,
        finishedAt = null,
        finishedAtPrecision = null,
        editionPages = null,
    )

    @Nested
    inner class ToDeadlineProgress {
        @Test
        fun `returns null without a deadline`() {
            // ----- Arrange -----
            val book = book()

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline = null)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null without a current edition`() {
            // ----- Arrange -----
            val book = book(hasEdition = false)

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline = deadline(unit = DeadlineUnit.PAGES))

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `a pages deadline counts the pages left against the edition pages`() {
            // ----- Arrange -----
            val book = book(read = read(page = 40))

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline = deadline(unit = DeadlineUnit.PAGES))

            // ----- Assert -----
            val progress = result.shouldNotBeNull()
            progress.unitsRemaining shouldBe 60
            progress.unit shouldBe DeadlineUnit.PAGES
        }

        @Test
        fun `a seconds deadline counts the seconds left against the audio length`() {
            // ----- Arrange -----
            val book = book(
                read = read(
                    seconds = 6000,
                    page = 99,
                ),
            )

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline = deadline(unit = DeadlineUnit.SECONDS))

            // ----- Assert -----
            val progress = result.shouldNotBeNull()
            progress.unitsRemaining shouldBe 30000
            progress.unit shouldBe DeadlineUnit.SECONDS
        }

        @Test
        fun `treats a missing read as zero progress`() {
            // ----- Arrange -----
            val book = book(read = null)

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline = deadline(unit = DeadlineUnit.PAGES))

            // ----- Assert -----
            result.shouldNotBeNull().unitsRemaining shouldBe 100
        }

        @Test
        fun `treats a missing total as nothing remaining`() {
            // ----- Arrange -----
            val book = book(pages = null)

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline = deadline(unit = DeadlineUnit.PAGES))

            // ----- Assert -----
            result.shouldNotBeNull().unitsRemaining shouldBe 0
        }
    }
}
