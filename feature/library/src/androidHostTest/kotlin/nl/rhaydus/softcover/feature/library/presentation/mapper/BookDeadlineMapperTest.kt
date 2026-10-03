package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock

class BookDeadlineMapperTest {
    private fun deadline(unit: DeadlineUnit): BookDeadline {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())

        return BookDeadline(
            bookId = 1,
            deadlineDate = today.plus(DatePeriod(days = 10)),
            setAt = today,
            initialPerDay = 10f,
            unit = unit,
        )
    }

    private fun stubBook(
        pages: Int? = null,
        audioSeconds: Int? = null,
        currentPage: Int? = null,
        currentSeconds: Int? = null,
        hasEdition: Boolean = true,
        hasRead: Boolean = true,
    ): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.currentEdition
        } returns if (hasEdition) {
            mockk<BookEdition>(relaxed = true).also { edition ->
                every {
                    edition.pages
                } returns pages

                every {
                    edition.audioSeconds
                } returns audioSeconds
            }
        } else {
            null
        }

        every {
            book.userBookRead
        } returns if (hasRead) {
            mockk<UserBookRead>(relaxed = true).also { read ->
                every {
                    read.currentPage
                } returns currentPage

                every {
                    read.currentSeconds
                } returns currentSeconds
            }
        } else {
            null
        }
    }

    @Nested
    inner class ToDeadlineProgress {
        @Test
        fun `a pages deadline counts the pages left`() {
            // ----- Arrange -----
            val book = stubBook(
                pages = 100,
                currentPage = 40,
                audioSeconds = 9_999,
                currentSeconds = 1,
            )

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline(DeadlineUnit.PAGES))

            // ----- Assert -----
            val progress = result.shouldNotBeNull()
            progress.unitsRemaining shouldBe 60
            progress.unit shouldBe DeadlineUnit.PAGES
        }

        @Test
        fun `a seconds deadline counts the audio seconds left`() {
            // ----- Arrange -----
            val book = stubBook(
                pages = 100,
                currentPage = 40,
                audioSeconds = 3_600,
                currentSeconds = 600,
            )

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline(DeadlineUnit.SECONDS))

            // ----- Assert -----
            val progress = result.shouldNotBeNull()
            progress.unitsRemaining shouldBe 3_000
            progress.unit shouldBe DeadlineUnit.SECONDS
        }

        @Test
        fun `a missing read record counts from zero`() {
            // ----- Arrange -----
            val book = stubBook(
                pages = 100,
                hasRead = false,
            )

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline(DeadlineUnit.PAGES))

            // ----- Assert -----
            result.shouldNotBeNull().unitsRemaining shouldBe 100
        }

        @Test
        fun `a missing total counts as nothing left`() {
            // ----- Arrange -----
            val book = stubBook(
                pages = null,
                currentPage = 10,
            )

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline(DeadlineUnit.PAGES))

            // ----- Assert -----
            result.shouldNotBeNull().unitsRemaining shouldBe 0
        }

        @Test
        fun `no current edition yields no progress`() {
            // ----- Arrange -----
            val book = stubBook(hasEdition = false)

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline(DeadlineUnit.PAGES))

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `no deadline yields no progress`() {
            // ----- Arrange -----
            val book = stubBook(pages = 100)

            // ----- Act -----
            val result = book.toDeadlineProgress(deadline = null)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
