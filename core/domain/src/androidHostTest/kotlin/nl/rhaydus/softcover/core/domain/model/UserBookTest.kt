package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import java.util.TimeZone
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserBookTest {
    private lateinit var originalZone: TimeZone

    @BeforeEach
    fun setUp() {
        originalZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Amsterdam"))
    }

    @AfterEach
    fun tearDown() {
        TimeZone.setDefault(originalZone)
    }

    private fun buildUserBook(
        journals: List<ReadingJournal>,
        reads: List<UserBookRead> = emptyList(),
        status: BookStatus = BookStatus.Read,
        lastReadDate: String? = null,
    ): UserBook = UserBook(
        id = 1,
        status = status,
        dateAdded = "2026-01-01",
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = lastReadDate,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = journals,
        reads = reads,
    )

    private fun journal(
        event: JournalEventType,
        updatedAt: String,
    ): ReadingJournal = ReadingJournal(
        updatedAt = updatedAt,
        event = event.eventName,
    )

    private fun read(
        id: Int,
        finishedAt: String? = null,
    ): UserBookRead = UserBookRead(
        id = id,
        currentPage = null,
        currentSeconds = null,
        progress = 0f,
        startedAt = null,
        finishedAt = finishedAt,
    )

    @Nested
    inner class CurrentRead {
        @Test
        fun `reading picks the newest unfinished read`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Reading,
                reads = listOf(read(id = 1), read(id = 3), read(
                    id = 2,
                    finishedAt = "2026-01-01",
                ),),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe 3
        }

        @Test
        fun `reading prefers an older unfinished read over a newer finished one`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Reading,
                reads = listOf(read(id = 1), read(
                    id = 2,
                    finishedAt = "2026-01-01",
                ),),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe 1
        }

        @Test
        fun `reading with only finished reads falls back to the newest read`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Reading,
                reads = listOf(
                    read(
                        id = 1,
                        finishedAt = "2026-01-01",
                    ),
                    read(
                        id = 2,
                        finishedAt = "2026-02-01",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe 2
        }

        @Test
        fun `paused behaves like reading`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Paused,
                reads = listOf(read(id = 1), read(
                    id = 2,
                    finishedAt = "2026-01-01",
                ),),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe 1
        }

        @Test
        fun `a placeholder unfinished read beats a higher-id unfinished server read`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Reading,
                reads = listOf(read(id = 9), read(id = -1)),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe -1
        }

        @Test
        fun `read picks the newest finished read by finishedAt even with a lower id`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Read,
                reads = listOf(
                    read(
                        id = 1,
                        finishedAt = "2026-03-01",
                    ),
                    read(
                        id = 2,
                        finishedAt = "2026-02-01",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe 1
        }

        @Test
        fun `read breaks a finishedAt tie by the higher id`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Read,
                reads = listOf(
                    read(
                        id = 2,
                        finishedAt = "2026-03-01",
                    ),
                    read(
                        id = 5,
                        finishedAt = "2026-03-01",
                    ),
                    read(
                        id = 3,
                        finishedAt = "2026-03-01",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe 5
        }

        @Test
        fun `read without a finished read falls back to the newest read`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Read,
                reads = listOf(read(id = 1), read(id = 4)),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe 4
        }

        @Test
        fun `did not finish picks the newest read even when finished`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.DidNotFinish,
                reads = listOf(read(id = 1), read(
                    id = 2,
                    finishedAt = "2026-01-01",
                ),),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe 2
        }

        @Test
        fun `want to read has no current read even with reads`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.WantToRead,
                reads = listOf(read(id = 1)),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `none has no current read even with reads`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.None,
                reads = listOf(read(id = 1)),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `reading with no reads has no current read`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Reading,
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `read with equal finish dates prefers the placeholder over the server read`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                status = BookStatus.Read,
                reads = listOf(
                    read(
                        id = 7,
                        finishedAt = "2026-03-01",
                    ),
                    read(
                        id = -1,
                        finishedAt = "2026-03-01",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.currentRead

            // ----- Assert -----
            result?.id shouldBe -1
        }
    }

    @Nested
    inner class ReadHistory {
        @Test
        fun `placeholders come first then ids descending`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                reads = listOf(read(id = 2), read(id = -1), read(id = 7), read(id = -2)),
            )

            // ----- Act -----
            val result = userBook.readHistory.map { it.id }

            // ----- Assert -----
            result shouldBe listOf(-1, -2, 7, 2)
        }
    }

    @Nested
    inner class FinishedReadCount {
        @Test
        fun `counts only reads with a finishedAt`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                reads = listOf(read(
                    id = 1,
                    finishedAt = "2026-01-01",
                ), read(id = 2), read(
                    id = 3,
                    finishedAt = "2025-01-01",
                ),),
            )

            // ----- Act -----
            val result = userBook.finishedReadCount

            // ----- Assert -----
            result shouldBe 2
        }

        @Test
        fun `no reads counts zero`() {
            // ----- Arrange -----
            val userBook = buildUserBook(journals = emptyList())

            // ----- Act -----
            val result = userBook.finishedReadCount

            // ----- Assert -----
            result shouldBe 0
        }
    }

    @Nested
    inner class FinishYears {
        @Test
        fun `years are distinct and ascending`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                reads = listOf(
                    read(
                        id = 1,
                        finishedAt = "2026-05-01",
                    ),
                    read(
                        id = 2,
                        finishedAt = "2024-01-01",
                    ),
                    read(
                        id = 3,
                        finishedAt = "2026-01-01",
                    ),
                    read(id = 4),
                ),
            )

            // ----- Act -----
            val result = userBook.finishYears

            // ----- Assert -----
            result shouldBe listOf(2024, 2026)
        }

        @Test
        fun `an unparseable finishedAt is skipped`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                reads = listOf(read(
                    id = 1,
                    finishedAt = "garbage",
                ), read(
                    id = 2,
                    finishedAt = "2025-02-02",
                ),),
            )

            // ----- Act -----
            val result = userBook.finishYears

            // ----- Assert -----
            result shouldBe listOf(2025)
        }
    }

    @Nested
    inner class FinishDate {
        private val journalFinished = journal(
            event = JournalEventType.StatusFinished,
            updatedAt = "2026-03-10T12:00:00.000000",
        )

        @Test
        fun `the newest finished read wins when it is the latest source`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = listOf(journalFinished),
                lastReadDate = "2026-03-11",
                reads = listOf(read(
                    id = 1,
                    finishedAt = "2026-04-01",
                ),),
            )

            // ----- Act -----
            val result = userBook.finishDate

            // ----- Assert -----
            result shouldBe LocalDate(
                2026,
                4,
                1,
            )
        }

        @Test
        fun `lastReadDate wins when it is the latest source`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = listOf(journalFinished),
                lastReadDate = "2026-05-01",
                reads = listOf(read(
                    id = 1,
                    finishedAt = "2026-04-01",
                ),),
            )

            // ----- Act -----
            val result = userBook.finishDate

            // ----- Assert -----
            result shouldBe LocalDate(
                2026,
                5,
                1,
            )
        }

        @Test
        fun `the finished journal wins when it is the latest source`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = listOf(journalFinished),
                lastReadDate = "2026-03-01",
                reads = listOf(read(
                    id = 1,
                    finishedAt = "2026-02-01",
                ),),
            )

            // ----- Act -----
            val result = userBook.finishDate

            // ----- Assert -----
            result shouldBe LocalDate(
                2026,
                3,
                10,
            )
        }

        @Test
        fun `all sources missing returns null`() {
            // ----- Arrange -----
            val userBook = buildUserBook(journals = emptyList())

            // ----- Act -----
            val result = userBook.finishDate

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class GetReadDateString {
        @Test
        fun `a finished event in the UTC late evening shows the next local day`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = listOf(
                    journal(
                        event = JournalEventType.StatusFinished,
                        updatedAt = "2026-07-01T22:30:00.000000",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.getReadDateString(style = DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            result shouldBe "2026/07/02"
        }

        @Test
        fun `a zone behind UTC shows the previous day for an early UTC morning`() {
            // ----- Arrange -----
            TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Pago_Pago"))
            val userBook = buildUserBook(
                journals = listOf(
                    journal(
                        event = JournalEventType.StatusFinished,
                        updatedAt = "2026-07-01T05:00:00.000000",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.getReadDateString(style = DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            result shouldBe "2026/06/30"
        }

        @Test
        fun `the most recent finished event wins across mixed server and local timestamps`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = listOf(
                    journal(
                        event = JournalEventType.StatusFinished,
                        updatedAt = "2026-07-01T10:00:00.123456",
                    ),
                    journal(
                        event = JournalEventType.StatusFinished,
                        updatedAt = "2026-07-03T22:30:00.000000",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.getReadDateString(style = DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            result shouldBe "2026/07/04"
        }

        @Test
        fun `a later finished read takes precedence over an earlier journal`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = listOf(
                    journal(
                        event = JournalEventType.StatusFinished,
                        updatedAt = "2026-07-01T22:30:00.000000",
                    ),
                ),
                reads = listOf(
                    UserBookRead(
                        id = 1,
                        currentSeconds = null,
                        currentPage = null,
                        progress = 100f,
                        startedAt = "2026-06-01",
                        finishedAt = "2026-07-09",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.getReadDateString(style = DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            result shouldBe "2026/07/09"
        }

        @Test
        fun `an unparseable journal timestamp returns null`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = listOf(
                    journal(
                        event = JournalEventType.StatusFinished,
                        updatedAt = "garbage",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.getReadDateString(style = DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `a null finish date returns null even with an unfinished read`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = emptyList(),
                reads = listOf(read(id = 1)),
            )

            // ----- Act -----
            val result = userBook.getReadDateString(style = DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `no finished event returns null`() {
            // ----- Arrange -----
            val userBook = buildUserBook(journals = emptyList())

            // ----- Act -----
            val result = userBook.getReadDateString(style = DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class GetDnfDateString {
        @Test
        fun `a did-not-finish event in the UTC late evening shows the next local day`() {
            // ----- Arrange -----
            val userBook = buildUserBook(
                journals = listOf(
                    journal(
                        event = JournalEventType.StatusDidNotFinish,
                        updatedAt = "2026-07-01T22:30:00.000000",
                    ),
                ),
            )

            // ----- Act -----
            val result = userBook.getDnfDateString(style = DateStyle.YEAR_MONTH_DAY)

            // ----- Assert -----
            result shouldBe "2026/07/02"
        }
    }
}
