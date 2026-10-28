package nl.rhaydus.softcover.core.book.data.repository

import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.book.baseBook
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DatePrecision
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Clock

class OptimisticBookTransformsTest {
    private val userBookId = 1

    private fun read(
        id: Int,
        progress: Float = 0f,
        page: Int? = null,
        startedAt: String? = "2026-01-01",
        finishedAt: String? = null,
    ): UserBookRead = UserBookRead(
        id = id,
        currentPage = page,
        currentSeconds = null,
        progress = progress,
        startedAt = startedAt,
        finishedAt = finishedAt,
        finishedAtPrecision = null,
        editionPages = null,
    )

    private fun bookWith(
        status: BookStatus,
        vararg reads: UserBookRead,
    ): Book {
        val userBook: UserBook = baseBook.userBook.shouldNotBeNull()

        return baseBook.copy(
            userBook = userBook.copy(
                id = userBookId,
                status = status,
                reads = reads.toList(),
            ),
        )
    }

    private fun Book.readsOrFail(): List<UserBookRead> = userBook.shouldNotBeNull().reads

    @Nested
    inner class WithMarkedAsReading {
        @Test
        fun `from WantToRead, Read and DidNotFinish appends a placeholder read that becomes current`() {
            listOf(BookStatus.WantToRead, BookStatus.Read, BookStatus.DidNotFinish).forEach { status ->
                // ----- Arrange -----
                val older = read(
                    id = 4,
                    progress = 100f,
                    finishedAt = "2025-06-01",
                )
                val book = bookWith(
                    status,
                    older,
                )

                // ----- Act -----
                val result = book.withMarkedAsReading()

                // ----- Assert -----
                val reads = result.readsOrFail()
                reads.size shouldBe 2
                reads.first { it.id == 4 } shouldBe older

                val placeholder = result.currentRead.shouldNotBeNull()
                placeholder.id shouldBe -userBookId
                placeholder.isPlaceholder shouldBe true
                placeholder.progress shouldBe 0f
                placeholder.finishedAt shouldBe null
                placeholder.startedAt shouldBe Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
                result.status shouldBe BookStatus.Reading
            }
        }

        @Test
        fun `from Paused with an unfinished read leaves the reads unchanged and keeps that read current`() {
            // ----- Arrange -----
            val finished = read(
                id = 4,
                progress = 100f,
                finishedAt = "2025-06-01",
            )
            val unfinished = read(
                id = 6,
                progress = 40f,
                page = 40,
            )
            val book = bookWith(
                BookStatus.Paused,
                finished,
                unfinished,
            )

            // ----- Act -----
            val result = book.withMarkedAsReading()

            // ----- Assert -----
            result.readsOrFail() shouldBe listOf(finished, unfinished)
            result.currentRead shouldBe unfinished
            result.status shouldBe BookStatus.Reading
        }

        @Test
        fun `from Paused with only finished reads appends a placeholder`() {
            // ----- Arrange -----
            val finished = read(
                id = 4,
                progress = 100f,
                finishedAt = "2025-06-01",
            )
            val book = bookWith(
                BookStatus.Paused,
                finished,
            )

            // ----- Act -----
            val result = book.withMarkedAsReading()

            // ----- Assert -----
            result.readsOrFail().size shouldBe 2
            result.currentRead.shouldNotBeNull().id shouldBe -userBookId
            result.readsOrFail().first { it.id == 4 } shouldBe finished
        }

        @Test
        fun `without a user book returns the book unchanged`() {
            // ----- Arrange -----
            val book = baseBook.copy(userBook = null)

            // ----- Act -----
            val result = book.withMarkedAsReading()

            // ----- Assert -----
            result shouldBe book
        }
    }

    @Nested
    inner class WithMarkedAsRead {
        @Test
        fun `from Reading and Paused finishes the unfinished current read at 100 percent`() {
            listOf(BookStatus.Reading, BookStatus.Paused).forEach { status ->
                // ----- Arrange -----
                val older = read(
                    id = 4,
                    progress = 100f,
                    finishedAt = "2025-06-01",
                )
                val current = read(
                    id = 6,
                    progress = 40f,
                    page = 40,
                )
                val book = bookWith(
                    status,
                    older,
                    current,
                )

                // ----- Act -----
                val result = book.withMarkedAsRead(actionAt = "2026-05-01T10:00:00Z")

                // ----- Assert -----
                val reads = result.readsOrFail()
                reads.first { it.id == 4 } shouldBe older
                val finished = reads.first { it.id == 6 }
                finished.finishedAt shouldBe "2026-05-01"
                finished.progress shouldBe 100f
                finished.currentPage shouldBe 40
                result.status shouldBe BookStatus.Read
            }
        }

        @Test
        fun `the finished read is dated to the day`() {
            // ----- Arrange -----
            val current = read(
                id = 6,
                progress = 40f,
                page = 40,
            )
            val book = bookWith(
                BookStatus.Reading,
                current,
            )

            // ----- Act -----
            val result = book.withMarkedAsRead(actionAt = "2026-05-01T10:00:00Z")

            // ----- Assert -----
            result.readsOrFail().single().finishedAtPrecision shouldBe DatePrecision.DAY
        }

        @Test
        fun `from WantToRead and DidNotFinish keeps the reads and adds a finished placeholder`() {
            listOf(BookStatus.WantToRead, BookStatus.DidNotFinish).forEach { status ->
                // ----- Arrange -----
                val unfinished = read(
                    id = 6,
                    progress = 40f,
                )
                val book = bookWith(
                    status,
                    unfinished,
                )

                // ----- Act -----
                val result = book.withMarkedAsRead(actionAt = "2026-05-01T10:00:00Z")

                // ----- Assert -----
                val reads = result.readsOrFail()
                reads.size shouldBe 2
                reads.first { it.id == 6 } shouldBe unfinished
                val placeholder = reads.first { it.id == -userBookId }
                placeholder.startedAt shouldBe null
                placeholder.finishedAt shouldBe "2026-05-01"
                placeholder.progress shouldBe 100f
                result.status shouldBe BookStatus.Read
            }
        }

        @Test
        fun `from Reading whose current read is already finished keeps it and adds a finished placeholder`() {
            // ----- Arrange -----
            val alreadyFinished = read(
                id = 6,
                progress = 100f,
                finishedAt = "2025-06-01",
            )
            val book = bookWith(
                BookStatus.Reading,
                alreadyFinished,
            )

            // ----- Act -----
            val result = book.withMarkedAsRead(actionAt = "2026-05-01T10:00:00Z")

            // ----- Assert -----
            val reads = result.readsOrFail()
            reads.size shouldBe 2
            reads.first { it.id == 6 } shouldBe alreadyFinished
            val placeholder = reads.first { it.id == -userBookId }
            placeholder.startedAt shouldBe null
            placeholder.finishedAt shouldBe "2026-05-01"
            placeholder.progress shouldBe 100f
            result.status shouldBe BookStatus.Read
        }

        @Test
        fun `from WantToRead dates the book to the marked day`() {
            // ----- Arrange -----
            val book = bookWith(BookStatus.WantToRead)

            // ----- Act -----
            val result = book.withMarkedAsRead(actionAt = "2026-05-01T10:00:00Z")

            // ----- Assert -----
            result.userBook.shouldNotBeNull().finishDate.toString() shouldBe "2026-05-01"
        }

        @Test
        fun `without an actionAt finishes the read today`() {
            // ----- Arrange -----
            val book = bookWith(
                BookStatus.Reading,
                read(
                    id = 6,
                    progress = 40f,
                ),
            )

            // ----- Act -----
            val result = book.withMarkedAsRead(actionAt = null)

            // ----- Assert -----
            result.readsOrFail().single().finishedAt shouldBe
                Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        }
    }

    @Nested
    inner class WithProgress {
        @Test
        fun `updates only the current read and leaves other reads unchanged`() {
            // ----- Arrange -----
            val older = read(
                id = 4,
                progress = 100f,
                page = 100,
                finishedAt = "2025-06-01",
            )
            val current = read(
                id = 6,
                progress = 10f,
                page = 10,
            )
            val book = bookWith(
                BookStatus.Reading,
                older,
                current,
            )

            // ----- Act -----
            val result = book.withProgress(
                newPage = 50,
                newSeconds = null,
            )

            // ----- Assert -----
            val reads = result.readsOrFail()
            reads.first { it.id == 4 } shouldBe older
            val updated = reads.first { it.id == 6 }
            updated.currentPage shouldBe 50
            updated.progress shouldBe 50f
        }

        @Test
        fun `without a current read returns the book unchanged`() {
            // ----- Arrange -----
            val book = bookWith(BookStatus.WantToRead)

            // ----- Act -----
            val result = book.withProgress(
                newPage = 50,
                newSeconds = null,
            )

            // ----- Assert -----
            result shouldBe book
        }
    }
}
