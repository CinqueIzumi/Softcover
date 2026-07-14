package nl.rhaydus.softcover.feature.book_detail.data.mapper

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.book_detail.domain.model.ReadingJournalEntry

class ReadingJournalEntryMapperTest {
    private fun validPagesMetadata(pages: Any? = 120): Map<String, Any?> = mapOf(
        "progress_pages" to pages,
    )

    private fun validSecondsMetadata(seconds: Any? = 900): Map<String, Any?> = mapOf(
        "progress_seconds" to seconds,
    )

    @Nested
    inner class ToReadingJournalEntry {
        @Test
        fun `maps valid progress_pages metadata into pages with seconds null`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = validPagesMetadata(pages = 120)

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe ReadingJournalEntry(
                date = LocalDate(
                    2024,
                    1,
                    1,
                ),
                pages = 120,
                seconds = null,
            )
        }

        @Test
        fun `maps valid progress_seconds metadata into seconds with pages null`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = validSecondsMetadata(seconds = 900)

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe ReadingJournalEntry(
                date = LocalDate(
                    2024,
                    1,
                    1,
                ),
                pages = null,
                seconds = 900,
            )
        }

        @Test
        fun `maps both progress_pages and progress_seconds when both present`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = mapOf(
                "progress_pages" to 50,
                "progress_seconds" to 600,
            )

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe ReadingJournalEntry(
                date = LocalDate(
                    2024,
                    1,
                    1,
                ),
                pages = 50,
                seconds = 600,
            )
        }

        @Test
        fun `parses a string-typed progress_pages value via toIntOrNull`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = validPagesMetadata(pages = "120")

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe ReadingJournalEntry(
                date = LocalDate(
                    2024,
                    1,
                    1,
                ),
                pages = 120,
                seconds = null,
            )
        }

        @Test
        fun `returns null when progress_pages is a non-numeric string and progress_seconds is absent`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = validPagesMetadata(pages = "abc")

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when progress_pages is a boolean and progress_seconds is absent`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = validPagesMetadata(pages = true)

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when metadata is a List rather than a Map`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = listOf("progress_pages" to 120)

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when metadata is a plain String`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = "not a map"

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when metadata is null`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = null,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `returns null when metadata map is missing both expected keys`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = mapOf("some_other_key" to 42)

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe null
        }

        @Test
        fun `parses a date-only updatedAt value into the date field`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01"
            val metadata = validPagesMetadata(pages = 10)

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result?.date shouldBe LocalDate(
                2024,
                1,
                1,
            )
        }

        @Test
        fun `parses a datetime updatedAt value taking only the date portion`() {
            // ----- Arrange -----
            val updatedAt = "2024-01-01T10:30:00"
            val metadata = validPagesMetadata(pages = 10)

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result?.date shouldBe LocalDate(
                2024,
                1,
                1,
            )
        }

        @Test
        fun `returns null when updatedAt fails to parse as any date format`() {
            // ----- Arrange -----
            val updatedAt = "not-a-date"
            val metadata = validPagesMetadata(pages = 10)

            // ----- Act -----
            val result = toReadingJournalEntry(
                updatedAt = updatedAt,
                metadata = metadata,
            )

            // ----- Assert -----
            result shouldBe null
        }
    }
}
