package nl.rhaydus.softcover.core.profile.domain.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LongestWaitingMapperTest {
    private fun library(
        id: Int,
        status: BookStatus = BookStatus.WantToRead,
        added: String = "2026-01-01",
        reads: List<UserBookRead> = emptyList(),
    ): Book {
        val base = readBook(
            id = id,
            reads = reads,
        )

        return base.copy(
            userBook = base.userBook?.copy(
                status = status,
                dateAdded = added,
            ),
        )
    }

    private fun owned(
        bookId: Int,
        addedAt: String?,
        book: Book? = null,
        listBookId: Int = bookId * 10,
    ): ListBook = ListBook(
        listBookId = listBookId,
        listId = 1,
        bookId = bookId,
        editionId = 0,
        addedAt = addedAt,
        book = book,
    )

    private fun day(
        year: Int,
        month: Int,
        dayOfMonth: Int,
    ): LocalDate = LocalDate(
        year = year,
        month = month,
        day = dayOfMonth,
    )

    @Nested
    inner class OnList {
        @Test
        fun `only Want to read books qualify and the oldest wins`() {
            // ----- Arrange -----
            val books = listOf(
                library(
                    id = 1,
                    status = BookStatus.Read,
                    added = "2020-01-01",
                ),
                library(
                    id = 2,
                    added = "2024-05-01",
                ),
                library(
                    id = 3,
                    added = "2023-05-01",
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = books,
                ownedListBooks = emptyList(),
            )

            // ----- Assert -----
            listOf(result.onList?.book?.id, result.onList?.since) shouldBe listOf(
                3,
                day(
                    2023,
                    5,
                    1,
                ),
            )
        }

        @Test
        fun `since is the later of the added date and the latest read date`() {
            // ----- Arrange -----
            val books = listOf(
                library(
                    id = 1,
                    added = "2024-01-01",
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2024-03-01",
                            finishedAt = "2024-04-10",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = books,
                ownedListBooks = emptyList(),
            )

            // ----- Assert -----
            result.onList?.since shouldBe day(
                2024,
                4,
                10,
            )
        }

        @Test
        fun `a read from before the book was added does not move since earlier`() {
            // ----- Arrange -----
            val books = listOf(
                library(
                    id = 1,
                    added = "2024-06-01",
                    reads = listOf(
                        datedRead(
                            id = 1,
                            startedAt = "2020-03-01",
                            finishedAt = "2020-04-10",
                        ),
                    ),
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = books,
                ownedListBooks = emptyList(),
            )

            // ----- Assert -----
            result.onList?.since shouldBe day(
                2024,
                6,
                1,
            )
        }

        @Test
        fun `a tie goes to the lower book id`() {
            // ----- Arrange -----
            val books = listOf(
                library(
                    id = 9,
                    added = "2024-05-01",
                ),
                library(
                    id = 4,
                    added = "2024-05-01",
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = books,
                ownedListBooks = emptyList(),
            )

            // ----- Assert -----
            result.onList?.book?.id shouldBe 4
        }

        @Test
        fun `no Want to read book leaves it empty`() {
            // ----- Arrange -----
            val books = listOf(
                library(
                    id = 1,
                    status = BookStatus.Read,
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = books,
                ownedListBooks = emptyList(),
            )

            // ----- Assert -----
            result.onList shouldBe null
        }
    }

    @Nested
    inner class OnShelf {
        @Test
        fun `an owned book that is Reading, Paused or Did not finish is not waiting`() {
            listOf(BookStatus.Reading, BookStatus.Paused, BookStatus.DidNotFinish).forEach { status ->
                // ----- Arrange -----
                val books = listOf(
                    library(
                        id = 1,
                        status = status,
                    ),
                )

                val rows = listOf(
                    owned(
                        bookId = 1,
                        addedAt = "2025-02-03",
                    ),
                )

                // ----- Act -----
                val result = toLongestWaiting(
                    books = books,
                    ownedListBooks = rows,
                )

                // ----- Assert -----
                result.onShelf shouldBe null
            }
        }

        @Test
        fun `since is the earliest parseable Owned date across editions`() {
            // ----- Arrange -----
            val books = listOf(
                library(
                    id = 1,
                    status = BookStatus.Read,
                ).let { it.copy(userBook = null) },
            )

            val rows = listOf(
                owned(
                    bookId = 1,
                    addedAt = "2026-06-15T11:52:00.854855",
                    listBookId = 1,
                ),
                owned(
                    bookId = 1,
                    addedAt = "2025-02-03",
                    listBookId = 2,
                ),
                owned(
                    bookId = 1,
                    addedAt = "2027-01-01T10:00:00+02:00",
                    listBookId = 3,
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = books,
                ownedListBooks = rows,
            )

            // ----- Assert -----
            result.onShelf?.since shouldBe day(
                2025,
                2,
                3,
            )
        }

        @Test
        fun `parses an offsetless date-time, a plain date and an offset form`() {
            // ----- Arrange -----
            val rows = listOf(
                owned(
                    bookId = 1,
                    addedAt = "2026-06-15T11:52:00.854855",
                    book = library(id = 1).copy(userBook = null),
                ),
                owned(
                    bookId = 2,
                    addedAt = "2026-06-16",
                    book = library(id = 2).copy(userBook = null),
                ),
                owned(
                    bookId = 3,
                    addedAt = "2026-06-17T23:30:00+02:00",
                    book = library(id = 3).copy(userBook = null),
                ),
            )

            // ----- Act -----
            val sinceByBook = (1..3).map { id ->
                toLongestWaiting(
                    books = emptyList(),
                    ownedListBooks = rows.filter { it.bookId == id },
                ).onShelf?.since
            }

            // ----- Assert -----
            sinceByBook shouldBe listOf(
                day(
                    2026,
                    6,
                    15,
                ),
                day(
                    2026,
                    6,
                    16,
                ),
                day(
                    2026,
                    6,
                    17,
                ),
            )
        }

        @Test
        fun `unparseable and null dates are skipped`() {
            // ----- Arrange -----
            val book = library(id = 1).copy(userBook = null)

            val rows = listOf(
                owned(
                    bookId = 1,
                    addedAt = "garbage",
                    book = book,
                    listBookId = 1,
                ),
                owned(
                    bookId = 1,
                    addedAt = null,
                    book = book,
                    listBookId = 2,
                ),
                owned(
                    bookId = 1,
                    addedAt = "2025-02-03",
                    book = book,
                    listBookId = 3,
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = emptyList(),
                ownedListBooks = rows,
            )

            // ----- Assert -----
            result.onShelf?.since shouldBe day(
                2025,
                2,
                3,
            )
        }

        @Test
        fun `a book with no parseable date at all is left out`() {
            // ----- Arrange -----
            val rows = listOf(
                owned(
                    bookId = 1,
                    addedAt = null,
                    book = library(id = 1).copy(userBook = null),
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = emptyList(),
                ownedListBooks = rows,
            )

            // ----- Assert -----
            result.onShelf shouldBe null
        }

        @Test
        fun `a book with a started read is excluded`() {
            // ----- Arrange -----
            val started = library(
                id = 1,
                status = BookStatus.Reading,
                reads = listOf(
                    datedRead(
                        id = 1,
                        startedAt = "2026-01-01",
                        finishedAt = null,
                    ),
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = listOf(started),
                ownedListBooks = listOf(
                    owned(
                        bookId = 1,
                        addedAt = "2020-01-01",
                    ),
                ),
            )

            // ----- Assert -----
            result.onShelf shouldBe null
        }

        @Test
        fun `a book with Read status is excluded even when no read has a start`() {
            // ----- Arrange -----
            val finished = library(
                id = 1,
                status = BookStatus.Read,
                reads = listOf(
                    finishedRead(
                        id = 1,
                        finishedAt = "2026-01-01",
                    ),
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = listOf(finished),
                ownedListBooks = listOf(
                    owned(
                        bookId = 1,
                        addedAt = "2020-01-01",
                    ),
                ),
            )

            // ----- Assert -----
            result.onShelf shouldBe null
        }

        @Test
        fun `the book already shown as the on list wait is excluded`() {
            // ----- Arrange -----
            val wanted = library(
                id = 1,
                added = "2022-01-01",
            )

            val other = library(
                id = 2,
                status = BookStatus.WantToRead,
                added = "2025-01-01",
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = listOf(wanted, other),
                ownedListBooks = listOf(
                    owned(
                        bookId = 1,
                        addedAt = "2018-01-01",
                    ),
                    owned(
                        bookId = 2,
                        addedAt = "2019-01-01",
                    ),
                ),
            )

            // ----- Assert -----
            listOf(result.onList?.book?.id, result.onShelf?.book?.id) shouldBe listOf(1, 2)
        }

        @Test
        fun `a book outside the library falls back to the list book`() {
            // ----- Arrange -----
            val listed = library(id = 7).copy(userBook = null)

            // ----- Act -----
            val result = toLongestWaiting(
                books = emptyList(),
                ownedListBooks = listOf(
                    owned(
                        bookId = 7,
                        addedAt = "2021-04-04",
                        book = listed,
                    ),
                ),
            )

            // ----- Assert -----
            result.onShelf?.book shouldBe listed
        }

        @Test
        fun `a row with no library book and no list book is skipped`() {
            // ----- Arrange -----
            val rows = listOf(
                owned(
                    bookId = 7,
                    addedAt = "2021-04-04",
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = emptyList(),
                ownedListBooks = rows,
            )

            // ----- Assert -----
            result.onShelf shouldBe null
        }

        @Test
        fun `the oldest wins and a tie goes to the lower book id`() {
            // ----- Arrange -----
            val rows = listOf(
                owned(
                    bookId = 9,
                    addedAt = "2020-01-01",
                    book = library(id = 9).copy(userBook = null),
                ),
                owned(
                    bookId = 4,
                    addedAt = "2020-01-01T08:00:00.000000",
                    book = library(id = 4).copy(userBook = null),
                ),
                owned(
                    bookId = 2,
                    addedAt = "2021-01-01",
                    book = library(id = 2).copy(userBook = null),
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = emptyList(),
                ownedListBooks = rows,
            )

            // ----- Assert -----
            result.onShelf?.book?.id shouldBe 4
        }
    }

    @Nested
    inner class HasUndatedOwned {
        @Test
        fun `is true when an Owned row has no addedAt`() {
            // ----- Arrange -----
            val rows = listOf(
                owned(
                    bookId = 1,
                    addedAt = "2025-02-03",
                ),
                owned(
                    bookId = 2,
                    addedAt = null,
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = emptyList(),
                ownedListBooks = rows,
            )

            // ----- Assert -----
            result.hasUndatedOwned shouldBe true
        }

        @Test
        fun `is true when an Owned row has an unparseable addedAt`() {
            // ----- Arrange -----
            val rows = listOf(
                owned(
                    bookId = 1,
                    addedAt = "2025-02-03",
                ),
                owned(
                    bookId = 2,
                    addedAt = "garbage",
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = emptyList(),
                ownedListBooks = rows,
            )

            // ----- Assert -----
            result.hasUndatedOwned shouldBe true
        }

        @Test
        fun `is false when every Owned row is dated`() {
            // ----- Arrange -----
            val rows = listOf(
                owned(
                    bookId = 1,
                    addedAt = "2025-02-03",
                ),
                owned(
                    bookId = 2,
                    addedAt = "2026-06-15T11:52:00.854855",
                ),
            )

            // ----- Act -----
            val result = toLongestWaiting(
                books = emptyList(),
                ownedListBooks = rows,
            )

            // ----- Assert -----
            result.hasUndatedOwned shouldBe false
        }

        @Test
        fun `is false when there are no Owned rows`() {
            // ----- Arrange -----
            val rows = emptyList<ListBook>()

            // ----- Act -----
            val result = toLongestWaiting(
                books = emptyList(),
                ownedListBooks = rows,
            )

            // ----- Assert -----
            result.hasUndatedOwned shouldBe false
        }
    }
}
