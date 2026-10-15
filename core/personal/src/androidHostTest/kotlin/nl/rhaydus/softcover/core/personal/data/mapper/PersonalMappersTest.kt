package nl.rhaydus.softcover.core.personal.data.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.model.HighlightEntity
import nl.rhaydus.softcover.core.database.model.ReadingSessionEntity
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import nl.rhaydus.softcover.core.personal.domain.model.Highlight
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

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
        pausedSeconds: Int = 0,
        lastPausedAt: String? = null,
    ) = ReadingSessionEntity(
        id = id,
        bookId = bookId,
        startedAt = startedAt,
        endedAt = endedAt,
        startPage = startPage,
        endPage = endPage,
        startSeconds = startSeconds,
        endSeconds = endSeconds,
        pausedSeconds = pausedSeconds,
        lastPausedAt = lastPausedAt,
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
        fun `falls back to Instant EPOCH when createdAt is malformed`() {
            // ----- Arrange -----
            val entity = buildHighlightEntity(createdAt = "bad-date")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.createdAt shouldBe Instant.fromEpochMilliseconds(0)
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
        fun `maps every field of a completed paused session`() {
            // ----- Arrange -----
            val entity = buildReadingSessionEntity(
                endedAt = "2024-04-01T10:00:00Z",
                endPage = 80,
                pausedSeconds = 120,
                lastPausedAt = "2024-04-01T09:30:00Z",
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.id shouldBe 10L
            result.bookId shouldBe 1
            result.startedAt shouldBe Instant.parse("2024-04-01T09:00:00Z")
            result.endedAt shouldBe Instant.parse("2024-04-01T10:00:00Z")
            result.startPage shouldBe 50
            result.endPage shouldBe 80
            result.pausedSeconds shouldBe 120
            result.lastPausedAt shouldBe Instant.parse("2024-04-01T09:30:00Z")
        }

        @Test
        fun `falls back to Instant EPOCH when startedAt is malformed`() {
            // ----- Arrange -----
            val entity = buildReadingSessionEntity(startedAt = "bad")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.startedAt shouldBe Instant.fromEpochMilliseconds(0)
        }

        @Test
        fun `falls back to Instant EPOCH when endedAt is malformed`() {
            // ----- Arrange -----
            val entity = buildReadingSessionEntity(endedAt = "bad")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.endedAt shouldBe Instant.fromEpochMilliseconds(0)
        }

        @Test
        fun `maps a malformed lastPausedAt to null`() {
            // ----- Arrange -----
            val entity = buildReadingSessionEntity(lastPausedAt = "not-a-timestamp")

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.lastPausedAt shouldBe null
        }
    }

    @Nested
    inner class ReadingSessionToEntity {
        @Test
        fun `maps every field of a paused session to the entity`() {
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
                pausedSeconds = 180,
                lastPausedAt = Instant.parse("2024-04-01T09:20:00Z"),
            )

            // ----- Act -----
            val result = session.toEntity()

            // ----- Assert -----
            result.id shouldBe 10L
            result.bookId shouldBe 1
            result.startedAt shouldBe "2024-04-01T09:00:00Z"
            result.endedAt shouldBe null
            result.startPage shouldBe 50
            result.pausedSeconds shouldBe 180
            result.lastPausedAt shouldBe "2024-04-01T09:20:00Z"
        }
    }
}

