package nl.rhaydus.softcover.core.connectivity.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.model.PendingUserBookWriteEntity
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWrite
import nl.rhaydus.softcover.core.domain.connectivity.PendingUserBookWriteKind
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PendingUserBookWriteMapperTest {
    private fun write(kind: PendingUserBookWriteKind = PendingUserBookWriteKind.UPDATE_REVIEW) =
        PendingUserBookWrite(
            kind = kind,
            userBookId = 1,
            userBookReadId = 2,
            bookId = 3,
            editionId = 4,
            progressPages = 50,
            progressSeconds = 60,
            startedAt = "2026-01-01",
            finishedAt = "2026-01-02",
            rating = 4.5,
            reviewSlateJson = "[{}]",
            reviewHasSpoilers = true,
            enqueuedAt = "2026-01-03T00:00:00Z",
            actionAt = "2026-01-04T00:00:00Z",
        )

    private fun entity(kind: String = "UPDATE_REVIEW") = PendingUserBookWriteEntity(
        kind = kind,
        userBookId = 1,
        userBookReadId = 2,
        bookId = 3,
        editionId = 4,
        progressPages = 50,
        progressSeconds = 60,
        startedAt = "2026-01-01",
        finishedAt = "2026-01-02",
        rating = 4.5,
        reviewSlateJson = "[{}]",
        reviewHasSpoilers = true,
        enqueuedAt = "2026-01-03T00:00:00Z",
        actionAt = "2026-01-04T00:00:00Z",
    )

    @Nested
    inner class ToEntity {
        @Test
        fun `maps every field and names the kind by its enum name`() {
            // ----- Arrange -----
            val input = write(kind = PendingUserBookWriteKind.MARK_AS_READ)

            // ----- Act -----
            val result = input.toEntity()

            // ----- Assert -----
            result shouldBe entity(kind = "MARK_AS_READ")
        }

        @Test
        fun `keeps nullable fields null`() {
            // ----- Arrange -----
            val input = write().copy(
                editionId = null,
                progressPages = null,
                progressSeconds = null,
                startedAt = null,
                finishedAt = null,
                rating = null,
                reviewSlateJson = null,
                reviewHasSpoilers = null,
                actionAt = null,
            )

            // ----- Act -----
            val result = input.toEntity()

            // ----- Assert -----
            result shouldBe entity().copy(
                editionId = null,
                progressPages = null,
                progressSeconds = null,
                startedAt = null,
                finishedAt = null,
                rating = null,
                reviewSlateJson = null,
                reviewHasSpoilers = null,
                actionAt = null,
            )
        }
    }

    @Nested
    inner class ToPendingUserBookWrite {
        @Test
        fun `maps every field of a valid entity`() {
            // ----- Arrange -----
            val input = entity(kind = "UPDATE_RATING")

            // ----- Act -----
            val result = input.toPendingUserBookWrite()

            // ----- Assert -----
            result shouldBe write(kind = PendingUserBookWriteKind.UPDATE_RATING)
        }

        @Test
        fun `returns null for an unknown kind`() {
            // ----- Arrange -----
            val input = entity(kind = "FROM_THE_FUTURE")

            // ----- Act -----
            val result = input.toPendingUserBookWrite()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null for an empty kind`() {
            // ----- Arrange -----
            val input = entity(kind = "")

            // ----- Act -----
            val result = input.toPendingUserBookWrite()

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class RoundTrip {
        @Test
        fun `returns the original write for every kind`() {
            // ----- Arrange -----
            val inputs = PendingUserBookWriteKind.entries.map { write(kind = it) }

            // ----- Act -----
            val results = inputs.map { it.toEntity().toPendingUserBookWrite() }

            // ----- Assert -----
            results shouldBe inputs
        }
    }
}
