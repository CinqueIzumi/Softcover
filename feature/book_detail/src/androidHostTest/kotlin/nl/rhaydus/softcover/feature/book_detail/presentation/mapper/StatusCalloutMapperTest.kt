package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.callout.CalloutActionUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReason
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons
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
    }

    private fun stubUserBook(
        readDate: String? = null,
        dnfDate: String? = null,
        fallbackDate: String = "FALLBACK",
    ): UserBook = mockk<UserBook> {
        every {
            getReadDateString(style = STYLE)
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
            val result = book.toStatusCallout(
                dateStyle = STYLE,
                reasons = null,
            )

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
            val result = book.toStatusCallout(
                dateStyle = STYLE,
                reasons = null,
            )

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
            val result = book.toStatusCallout(
                dateStyle = STYLE,
                reasons = null,
            )

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
            val result = book.toStatusCallout(
                dateStyle = STYLE,
                reasons = null,
            )

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
            val result = book.toStatusCallout(
                dateStyle = STYLE,
                reasons = null,
            )

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
                ).toStatusCallout(
                    dateStyle = STYLE,
                    reasons = null,
                ).shouldNotBeNull().eyebrow
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
            val result = book.toStatusCallout(
                dateStyle = STYLE,
                reasons = null,
            )

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
            val result = book.toStatusCallout(
                dateStyle = STYLE,
                reasons = null,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class DidNotFinishReasonDetail {
        private fun detailOf(
            status: BookStatus,
            reasons: DidNotFinishReasons?,
        ): CalloutActionUiModel.Detail = stubBook(
            status = status,
            userBook = stubUserBook(),
        ).toStatusCallout(
            dateStyle = STYLE,
            reasons = reasons,
        ).shouldNotBeNull().callToAction.shouldBeInstanceOf<CalloutActionUiModel.Detail>()

        @Test
        fun `joins the picked reasons in sheet order regardless of how they were stored`() {
            // ----- Arrange -----
            val reasons = DidNotFinishReasons(
                bookId = 1,
                reasons = linkedSetOf(
                    DidNotFinishReason.LostInterest,
                    DidNotFinishReason.NotTheRightTime,
                ),
            )

            // ----- Act -----
            val detail = detailOf(
                status = BookStatus.DidNotFinish,
                reasons = reasons,
            )

            // ----- Assert -----
            detail.lines shouldBe listOf("Not the right time, Lost interest")
        }

        @Test
        fun `shows only the own words when no reason is picked`() {
            // ----- Arrange -----
            val reasons = DidNotFinishReasons(
                bookId = 1,
                ownWords = "The middle dragged",
            )

            // ----- Act -----
            val detail = detailOf(
                status = BookStatus.DidNotFinish,
                reasons = reasons,
            )

            // ----- Assert -----
            detail.lines shouldBe listOf("The middle dragged")
        }

        @Test
        fun `shows the reasons first and the own words as a second line`() {
            // ----- Arrange -----
            val reasons = DidNotFinishReasons(
                bookId = 1,
                reasons = setOf(DidNotFinishReason.TooSlow),
                ownWords = "The middle dragged",
            )

            // ----- Act -----
            val detail = detailOf(
                status = BookStatus.DidNotFinish,
                reasons = reasons,
            )

            // ----- Assert -----
            detail.lines shouldBe listOf("Too slow", "The middle dragged")
        }

        @Test
        fun `has no lines when nothing is saved`() {
            // ----- Arrange -----
            val storedEmpty = DidNotFinishReasons(bookId = 1)

            // ----- Act -----
            val saved = detailOf(
                status = BookStatus.DidNotFinish,
                reasons = storedEmpty,
            )

            val unloaded = detailOf(
                status = BookStatus.DidNotFinish,
                reasons = null,
            )

            // ----- Assert -----
            saved.lines shouldBe emptyList()
            unloaded.lines shouldBe emptyList()
        }

        @Test
        fun `other callouts carry no detail row even when reasons are saved`() {
            // ----- Arrange -----
            val reasons = DidNotFinishReasons(
                bookId = 1,
                reasons = setOf(DidNotFinishReason.TooSlow),
            )

            // ----- Act -----
            val callouts = listOf(BookStatus.Read, BookStatus.WantToRead).map { status ->
                stubBook(
                    status = status,
                    userBook = stubUserBook(),
                ).toStatusCallout(
                    dateStyle = STYLE,
                    reasons = reasons,
                ).shouldNotBeNull()
            }

            // ----- Assert -----
            callouts.map { it.callToAction } shouldBe listOf(null, null)
        }
    }
}
