package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
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

    private fun buildUserBook(journals: List<ReadingJournal>): UserBook = UserBook(
        id = 1,
        status = BookStatus.Read,
        dateAdded = "2026-01-01",
        createdAt = null,
        privacySettingId = 1,
        reviewHasSpoilers = false,
        editionId = null,
        lastReadDate = null,
        rating = null,
        referrerUserId = null,
        reviewedAt = null,
        updatedAt = null,
        journals = journals,
    )

    private fun journal(
        event: JournalEventType,
        updatedAt: String,
    ): ReadingJournal = ReadingJournal(
        updatedAt = updatedAt,
        event = event.eventName,
    )

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
        fun `a valid finishedAt date takes precedence over the journal`() {
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
            val result = userBook.getReadDateString(
                style = DateStyle.YEAR_MONTH_DAY,
                finishedAt = "2026-05-09",
            )

            // ----- Assert -----
            result shouldBe "2026/05/09"
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
