package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.domain.model.ReadDuration
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuickestReadMapperTest {
    private fun duration(
        readId: Int,
        days: Int,
        bookId: Int = readId,
        pages: Int? = null,
        finishedOn: String = "2026-03-10",
    ): ReadDuration {
        val finish = LocalDate.parse(finishedOn)

        return ReadDuration(
            bookId = bookId,
            readId = readId,
            startedOn = LocalDate.fromEpochDays(finish.toEpochDays() - (days - 1)),
            finishedOn = finish,
            pages = pages,
        )
    }

    private val books = (1..5).map { readBook(id = it) }

    @Nested
    inner class QuickestRead {
        @Test
        fun `the read with the fewest days wins and carries its book`() {
            // ----- Arrange -----
            val durations = listOf(
                duration(
                    readId = 1,
                    days = 9,
                ),
                duration(
                    readId = 2,
                    days = 2,
                ),
                duration(
                    readId = 3,
                    days = 5,
                ),
            )

            // ----- Act -----
            val result = durations.quickestRead(books)

            // ----- Assert -----
            listOf(result?.duration?.readId, result?.book?.id) shouldBe listOf(2, 2)
        }

        @Test
        fun `an empty list gives null`() {
            // ----- Arrange -----
            val durations = emptyList<ReadDuration>()

            // ----- Act -----
            val result = durations.quickestRead(books)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `a winner whose book is not in the list gives null`() {
            // ----- Arrange -----
            val durations = listOf(
                duration(
                    readId = 1,
                    days = 1,
                    bookId = 99,
                ),
                duration(
                    readId = 2,
                    days = 4,
                ),
            )

            // ----- Act -----
            val result = durations.quickestRead(books)

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `a day tie goes to the read with more pages`() {
            // ----- Arrange -----
            val durations = listOf(
                duration(
                    readId = 1,
                    days = 3,
                    pages = 100,
                ),
                duration(
                    readId = 2,
                    days = 3,
                    pages = 400,
                ),
            )

            // ----- Act -----
            val result = durations.quickestRead(books)

            // ----- Assert -----
            result?.duration?.readId shouldBe 2
        }

        @Test
        fun `a day tie ranks unknown pages below a known page count`() {
            // ----- Arrange -----
            val durations = listOf(
                duration(
                    readId = 1,
                    days = 3,
                    pages = null,
                ),
                duration(
                    readId = 2,
                    days = 3,
                    pages = 1,
                ),
            )

            // ----- Act -----
            val result = durations.quickestRead(books)

            // ----- Assert -----
            result?.duration?.readId shouldBe 2
        }

        @Test
        fun `a day and page tie goes to the later finish`() {
            // ----- Arrange -----
            val durations = listOf(
                duration(
                    readId = 1,
                    days = 3,
                    pages = 200,
                    finishedOn = "2026-05-01",
                ),
                duration(
                    readId = 2,
                    days = 3,
                    pages = 200,
                    finishedOn = "2026-01-01",
                ),
            )

            // ----- Act -----
            val result = durations.quickestRead(books)

            // ----- Assert -----
            result?.duration?.readId shouldBe 1
        }

        @Test
        fun `a full tie goes to the higher read id`() {
            // ----- Arrange -----
            val durations = listOf(
                duration(
                    readId = 2,
                    days = 3,
                    pages = 200,
                ),
                duration(
                    readId = 4,
                    days = 3,
                    pages = 200,
                ),
                duration(
                    readId = 1,
                    days = 3,
                    pages = 200,
                ),
            )

            // ----- Act -----
            val result = durations.quickestRead(books)

            // ----- Assert -----
            result?.duration?.readId shouldBe 4
        }
    }
}
