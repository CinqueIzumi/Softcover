package nl.rhaydus.softcover.core.personal.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.softcover.core.database.model.ProgressHistoryEntity
import nl.rhaydus.softcover.core.personal.domain.model.ReadingJournalEntry
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ReadingJournalEntryMapperTest {
    private val hasuraInstant = "2024-10-05T10:30:00.123456+00:00"

    private fun millis(instant: String): Long = Instant.parse(instant).toEpochMilliseconds()

    private fun entity(actionAt: String): ProgressHistoryEntity = ProgressHistoryEntity(
        bookId = 1,
        actionAtEpochMillis = millis(instant = actionAt),
        pages = 10,
        seconds = null,
        pending = false,
    )

    @Nested
    inner class ToProgressHistoryEntity {
        @Test
        fun `maps pages metadata into a synced entity for the book`() {
            // ----- Arrange -----
            val metadata = mapOf("progress_pages" to 120)

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 9,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            val entity = result.shouldNotBeNull()
            entity.bookId shouldBe 9
            entity.pages shouldBe 120
            entity.seconds shouldBe null
            entity.pending shouldBe false
        }

        @Test
        fun `pages read is the move from the previous page to the new page`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "progress_pages" to 120,
                "progress_pages_was" to 90,
            )

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldNotBeNull().pagesRead shouldBe 30
        }

        @Test
        fun `pages read counts from page zero when the previous page is missing`() {
            // ----- Arrange -----
            val metadata = mapOf("progress_pages" to 120)

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldNotBeNull().pagesRead shouldBe 120
        }

        @Test
        fun `pages read is zero for a seconds-only audiobook update`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "progress_seconds" to 900,
                "progress_seconds_was" to 300,
            )

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldNotBeNull().pagesRead shouldBe 0
        }

        @Test
        fun `pages read is zero when the position moved backwards`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "progress_pages" to 40,
                "progress_pages_was" to 90,
            )

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldNotBeNull().pagesRead shouldBe 0
        }

        @Test
        fun `parses a Hasura timestamptz with offset and fractional seconds into epoch millis`() {
            // ----- Arrange -----
            val metadata = mapOf("progress_pages" to 1)

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldNotBeNull().actionAtEpochMillis shouldBe millis(instant = "2024-10-05T10:30:00.123Z")
        }

        @Test
        fun `converts a non-UTC offset to the same epoch millis as its UTC equivalent`() {
            // ----- Arrange -----
            val metadata = mapOf("progress_pages" to 1)

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = "2024-10-05T12:30:00+02:00",
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldNotBeNull().actionAtEpochMillis shouldBe millis(instant = "2024-10-05T10:30:00Z")
        }

        @Test
        fun `maps seconds metadata with pages null`() {
            // ----- Arrange -----
            val metadata = mapOf("progress_seconds" to 900)

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            val entity = result.shouldNotBeNull()
            entity.pages shouldBe null
            entity.seconds shouldBe 900
        }

        @Test
        fun `maps both pages and seconds when both present`() {
            // ----- Arrange -----
            val metadata = mapOf(
                "progress_pages" to 50,
                "progress_seconds" to 600,
            )

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            val entity = result.shouldNotBeNull()
            entity.pages shouldBe 50
            entity.seconds shouldBe 600
        }

        @Test
        fun `parses string-typed numbers`() {
            // ----- Arrange -----
            val metadata = mapOf("progress_pages" to "120")

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldNotBeNull().pages shouldBe 120
        }

        @Test
        fun `drops the entry when the only value is a non-numeric string`() {
            // ----- Arrange -----
            val metadata = mapOf("progress_pages" to "abc")

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `drops the entry when the only value is a boolean`() {
            // ----- Arrange -----
            val metadata = mapOf("progress_pages" to true)

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `drops the entry when metadata is a list`() {
            // ----- Arrange -----
            val metadata = listOf("progress_pages" to 120)

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `drops the entry when metadata is null`() {
            // ----- Arrange -----
            val metadata: Any? = null

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `drops the entry when both expected keys are missing`() {
            // ----- Arrange -----
            val metadata = mapOf("some_other_key" to 42)

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = hasuraInstant,
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `drops the entry when the instant is unparseable`() {
            // ----- Arrange -----
            val metadata = mapOf("progress_pages" to 10)

            // ----- Act -----
            val result = toProgressHistoryEntity(
                bookId = 1,
                actionAt = "not-a-date",
                metadata = metadata,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class ToReadingJournalEntry {
        @Test
        fun `carries pages and seconds through with the local date`() {
            // ----- Arrange -----
            val source = ProgressHistoryEntity(
                bookId = 1,
                actionAtEpochMillis = millis(instant = "2024-10-05T10:30:00Z"),
                pages = 50,
                seconds = 600,
                pending = true,
            )

            // ----- Act -----
            val result = source.toReadingJournalEntry(timeZone = TimeZone.UTC)

            // ----- Assert -----
            result shouldBe ReadingJournalEntry(
                date = LocalDate(
                    2024,
                    10,
                    5,
                ),
                pages = 50,
                seconds = 600,
            )
        }

        @Test
        fun `keeps an instant of 22 30Z on the same day in UTC`() {
            // ----- Arrange -----
            val source = entity(actionAt = "2024-10-05T22:30:00Z")

            // ----- Act -----
            val result = source.toReadingJournalEntry(timeZone = TimeZone.UTC)

            // ----- Assert -----
            result.date shouldBe LocalDate(
                2024,
                10,
                5,
            )
        }

        @Test
        fun `moves an instant of 22 30Z to the next day in Europe Amsterdam`() {
            // ----- Arrange -----
            val source = entity(actionAt = "2024-10-05T22:30:00Z")

            // ----- Act -----
            val result = source.toReadingJournalEntry(timeZone = TimeZone.of("Europe/Amsterdam"))

            // ----- Assert -----
            result.date shouldBe LocalDate(
                2024,
                10,
                6,
            )
        }

        @Test
        fun `moves an instant of 03 00Z to the previous day in America New_York`() {
            // ----- Arrange -----
            val source = entity(actionAt = "2024-10-05T03:00:00Z")

            // ----- Act -----
            val result = source.toReadingJournalEntry(timeZone = TimeZone.of("America/New_York"))

            // ----- Assert -----
            result.date shouldBe LocalDate(
                2024,
                10,
                4,
            )
        }

        @Test
        fun `dates a Hasura offset instant with fractional seconds by the local day`() {
            // ----- Arrange -----
            val source = toProgressHistoryEntity(
                bookId = 1,
                actionAt = "2024-10-05T22:30:00.5+00:00",
                metadata = mapOf("progress_pages" to 1),
            ).shouldNotBeNull()

            // ----- Act -----
            val result = source.toReadingJournalEntry(timeZone = TimeZone.of("Europe/Amsterdam"))

            // ----- Assert -----
            result.date shouldBe LocalDate(
                2024,
                10,
                6,
            )
        }
    }
}
