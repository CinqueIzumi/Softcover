package nl.rhaydus.softcover.feature.personal.data.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.personal.data.model.HighlightEntity
import nl.rhaydus.softcover.feature.personal.data.model.ReadingLogEntryEntity
import nl.rhaydus.softcover.feature.personal.data.model.ReadingSessionEntity
import nl.rhaydus.softcover.feature.personal.domain.model.Highlight
import nl.rhaydus.softcover.feature.personal.domain.model.ReadingLogEntry
import nl.rhaydus.softcover.feature.personal.domain.model.ReadingSession
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class PersonalMappersTest {

    // region Fixtures

    private fun buildHighlightEntity(
        id: Long = 42L,
        bookId: Int = 1,
        quote: String = "A quote",
        page: Int? = 12,
        note: String? = "A note",
        createdAt: String = "2024-03-01T08:00:00Z",
    ) = HighlightEntity(
        id = id,
        bookId = bookId,
        quote = quote,
        page = page,
        note = note,
        createdAt = createdAt,
    )

    private fun buildHighlight(
        id: Long = 42L,
        bookId: Int = 1,
        quote: String = "A quote",
        page: Int? = 12,
        note: String? = "A note",
        createdAt: Instant = Instant.parse("2024-03-01T08:00:00Z"),
    ) = Highlight(
        id = id,
        bookId = bookId,
        quote = quote,
        page = page,
        note = note,
        createdAt = createdAt,
    )

    private fun buildReadingSessionEntity(
        id: Long = 10L,
        bookId: Int = 1,
        startedAt: String = "2024-04-01T09:00:00Z",
        endedAt: String? = null,
        startPage: Int? = 50,
        endPage: Int? = null,
        startSeconds: Int? = null,
        endSeconds: Int? = null,
    ) = ReadingSessionEntity(
        id = id,
        bookId = bookId,
        startedAt = startedAt,
        endedAt = endedAt,
        startPage = startPage,
        endPage = endPage,
        startSeconds = startSeconds,
        endSeconds = endSeconds,
    )

    private fun buildReadingLogEntryEntity(
        id: Long = 5L,
        bookId: Int = 1,
        startedAt: String? = "2024-01-01",
        finishedAt: String? = "2024-01-15",
        rating: Double? = 4.5,
        note: String? = "Enjoyable",
        createdAt: String = "2024-01-16T12:00:00Z",
    ) = ReadingLogEntryEntity(
        id = id,
        bookId = bookId,
        startedAt = startedAt,
        finishedAt = finishedAt,
        rating = rating,
        note = note,
        createdAt = createdAt,
    )

    // endregion

    @Nested
    inner class HighlightEntityToDomain {

        @Test
        fun `maps all fields correctly for a valid entity`() {
            // ----- Arrange -----
            val entity = buildHighlightEntity()

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.id shouldBe 42L
            result.bookId shouldBe 1
            result.quote shouldBe "A quote"
            result.page shouldBe 12
            result.note shouldBe "A note"
            result.createdAt shouldBe Instant.parse("2024-03-01T08:00:00Z")
        }

        @Test
        fun `maps null page and note correctly`() {
            // ----- Arrange -----
            val entity = buildHighlightEntity(
                page = null,
                note = null,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.page shouldBe null
            result.note shouldBe null
        }

        @Test
        fun `falls back to Instant EPOCH when createdAt is malformed`() {
            // ----- Arrange -----
            val entity = buildHighlightEntity(createdAt = "bad-date")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.createdAt shouldBe Instant.EPOCH
        }
    }

    @Nested
    inner class HighlightToEntity {

        @Test
        fun `maps all fields correctly for a valid domain model`() {
            // ----- Arrange -----
            val highlight = buildHighlight()

            // ----- Act -----
            val result = highlight.toEntity()

            // ----- Assert -----
            result.id shouldBe 42L
            result.bookId shouldBe 1
            result.quote shouldBe "A quote"
            result.page shouldBe 12
            result.note shouldBe "A note"
            result.createdAt shouldBe "2024-03-01T08:00:00Z"
        }
    }

    @Nested
    inner class ReadingSessionEntityToDomain {

        @Test
        fun `maps active session (no endedAt) correctly`() {
            // ----- Arrange -----
            val entity = buildReadingSessionEntity()

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.id shouldBe 10L
            result.bookId shouldBe 1
            result.startedAt shouldBe Instant.parse("2024-04-01T09:00:00Z")
            result.endedAt shouldBe null
            result.startPage shouldBe 50
            result.endPage shouldBe null
        }

        @Test
        fun `maps completed session with endedAt correctly`() {
            // ----- Arrange -----
            val entity = buildReadingSessionEntity(
                endedAt = "2024-04-01T10:00:00Z",
                endPage = 80,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.endedAt shouldBe Instant.parse("2024-04-01T10:00:00Z")
            result.endPage shouldBe 80
        }

        @Test
        fun `falls back to Instant EPOCH when startedAt is malformed`() {
            // ----- Arrange -----
            val entity = buildReadingSessionEntity(startedAt = "bad")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.startedAt shouldBe Instant.EPOCH
        }

        @Test
        fun `falls back to Instant EPOCH when endedAt is malformed`() {
            // ----- Arrange -----
            val entity = buildReadingSessionEntity(endedAt = "bad")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.endedAt shouldBe Instant.EPOCH
        }
    }

    @Nested
    inner class ReadingSessionToEntity {

        @Test
        fun `maps all fields correctly for an active session`() {
            // ----- Arrange -----
            val session = ReadingSession(
                id = 10L,
                bookId = 1,
                startedAt = Instant.parse("2024-04-01T09:00:00Z"),
                endedAt = null,
                startPage = 50,
                endPage = null,
                startSeconds = null,
                endSeconds = null,
            )

            // ----- Act -----
            val result = session.toEntity()

            // ----- Assert -----
            result.id shouldBe 10L
            result.bookId shouldBe 1
            result.startedAt shouldBe "2024-04-01T09:00:00Z"
            result.endedAt shouldBe null
            result.startPage shouldBe 50
        }
    }

    @Nested
    inner class ReadingLogEntryEntityToDomain {

        @Test
        fun `maps all fields correctly for a valid entity`() {
            // ----- Arrange -----
            val entity = buildReadingLogEntryEntity()

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.id shouldBe 5L
            result.bookId shouldBe 1
            result.startedAt shouldBe LocalDate.of(2024, 1, 1)
            result.finishedAt shouldBe LocalDate.of(2024, 1, 15)
            result.rating shouldBe 4.5
            result.note shouldBe "Enjoyable"
            result.createdAt shouldBe Instant.parse("2024-01-16T12:00:00Z")
        }

        @Test
        fun `maps null startedAt and finishedAt to null`() {
            // ----- Arrange -----
            val entity = buildReadingLogEntryEntity(
                startedAt = null,
                finishedAt = null,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.startedAt shouldBe null
            result.finishedAt shouldBe null
        }

        @Test
        fun `returns null for startedAt when string is malformed`() {
            // ----- Arrange -----
            val entity = buildReadingLogEntryEntity(startedAt = "not-a-date")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.startedAt shouldBe null
        }

        @Test
        fun `returns null for finishedAt when string is malformed`() {
            // ----- Arrange -----
            val entity = buildReadingLogEntryEntity(finishedAt = "not-a-date")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.finishedAt shouldBe null
        }

        @Test
        fun `falls back to Instant EPOCH when createdAt is malformed`() {
            // ----- Arrange -----
            val entity = buildReadingLogEntryEntity(createdAt = "bad")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.createdAt shouldBe Instant.EPOCH
        }
    }

    @Nested
    inner class ReadingLogEntryToEntity {

        @Test
        fun `maps all fields correctly for a valid domain model`() {
            // ----- Arrange -----
            val entry = ReadingLogEntry(
                id = 5L,
                bookId = 1,
                startedAt = LocalDate.of(2024, 1, 1),
                finishedAt = LocalDate.of(2024, 1, 15),
                rating = 4.5,
                note = "Enjoyable",
                createdAt = Instant.parse("2024-01-16T12:00:00Z"),
            )

            // ----- Act -----
            val result = entry.toEntity()

            // ----- Assert -----
            result.id shouldBe 5L
            result.bookId shouldBe 1
            result.startedAt shouldBe "2024-01-01"
            result.finishedAt shouldBe "2024-01-15"
            result.rating shouldBe 4.5
            result.note shouldBe "Enjoyable"
            result.createdAt shouldBe "2024-01-16T12:00:00Z"
        }

        @Test
        fun `maps null LocalDate fields to null strings`() {
            // ----- Arrange -----
            val entry = ReadingLogEntry(
                id = 5L,
                bookId = 1,
                startedAt = null,
                finishedAt = null,
                rating = null,
                note = null,
                createdAt = Instant.parse("2024-01-16T12:00:00Z"),
            )

            // ----- Act -----
            val result = entry.toEntity()

            // ----- Assert -----
            result.startedAt shouldBe null
            result.finishedAt shouldBe null
        }
    }
}
