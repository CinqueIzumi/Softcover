package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private val STYLE = DateStyle.entries.first()

class StatusCalloutMapperTest {
    private fun stubBook(
        status: BookStatus,
        userBook: UserBook?,
    ): Book = mockk<Book> {
        every {
            this@mockk.status
        } returns status

        every {
            this@mockk.userBook
        } returns userBook

        every {
            userBookRead
        } returns null
    }

    private fun stubUserBook(
        readDate: String? = null,
        dnfDate: String? = null,
        fallbackDate: String = "FALLBACK",
    ): UserBook = mockk<UserBook> {
        every {
            getReadDateString(
                style = STYLE,
                finishedAt = null,
            )
        } returns readDate

        every {
            getDnfDateString(style = STYLE)
        } returns dnfDate

        every {
            getFallbackDateString(style = STYLE)
        } returns fallbackDate
    }

    @Nested
    inner class ToStatusCallout {
        @Test
        fun `Read with a finished date mentions that date`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Read,
                userBook = stubUserBook(readDate = "READ_DATE"),
            )

            // ----- Act -----
            val result = book.toStatusCallout(dateStyle = STYLE)

            // ----- Assert -----
            val body = result.shouldNotBeNull().body.shouldNotBeNull()
            body.contains("READ_DATE") shouldBe true
            body.contains("FALLBACK") shouldBe false
        }

        @Test
        fun `Read without a finished date falls back to the library date`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Read,
                userBook = stubUserBook(readDate = null),
            )

            // ----- Act -----
            val result = book.toStatusCallout(dateStyle = STYLE)

            // ----- Assert -----
            val body = result.shouldNotBeNull().body.shouldNotBeNull()
            body.contains("in your library since FALLBACK") shouldBe true
        }

        @Test
        fun `DidNotFinish with a dnf date mentions that date`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.DidNotFinish,
                userBook = stubUserBook(dnfDate = "DNF_DATE"),
            )

            // ----- Act -----
            val result = book.toStatusCallout(dateStyle = STYLE)

            // ----- Assert -----
            val body = result.shouldNotBeNull().body.shouldNotBeNull()
            body.contains("DNF_DATE") shouldBe true
            body.contains("FALLBACK") shouldBe false
        }

        @Test
        fun `DidNotFinish without a dnf date falls back to the library date`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.DidNotFinish,
                userBook = stubUserBook(dnfDate = null),
            )

            // ----- Act -----
            val result = book.toStatusCallout(dateStyle = STYLE)

            // ----- Assert -----
            val body = result.shouldNotBeNull().body.shouldNotBeNull()
            body.contains("in your library since FALLBACK") shouldBe true
        }

        @Test
        fun `WantToRead mentions the date it was shelved`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.WantToRead,
                userBook = stubUserBook(),
            )

            // ----- Act -----
            val result = book.toStatusCallout(dateStyle = STYLE)

            // ----- Assert -----
            val body = result.shouldNotBeNull().body.shouldNotBeNull()
            body.contains("FALLBACK") shouldBe true
        }

        @Test
        fun `each status with a callout has its own eyebrow`() {
            // ----- Arrange -----
            val statuses = listOf(
                BookStatus.Read,
                BookStatus.DidNotFinish,
                BookStatus.WantToRead,
            )

            // ----- Act -----
            val eyebrows = statuses.map { status ->
                stubBook(
                    status = status,
                    userBook = stubUserBook(),
                ).toStatusCallout(dateStyle = STYLE).shouldNotBeNull().eyebrow
            }

            // ----- Assert -----
            eyebrows.toSet().size shouldBe statuses.size
        }

        @Test
        fun `Reading has no callout`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Reading,
                userBook = stubUserBook(),
            )

            // ----- Act -----
            val result = book.toStatusCallout(dateStyle = STYLE)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `a book without a user book has no callout`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Read,
                userBook = null,
            )

            // ----- Act -----
            val result = book.toStatusCallout(dateStyle = STYLE)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
