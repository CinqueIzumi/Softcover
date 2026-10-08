package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ReadingSessionTest {
    // region Fixtures
    private val start = Instant.parse("2024-06-01T10:00:00Z")
    private val end = Instant.parse("2024-06-01T11:00:00Z")

    private fun buildSession(
        startedAt: Instant = start,
        endedAt: Instant? = null,
        pausedSeconds: Int = 0,
        lastPausedAt: Instant? = null,
    ) = ReadingSession(
        id = 1L,
        bookId = 10,
        startedAt = startedAt,
        endedAt = endedAt,
        startPage = null,
        endPage = null,
        startSeconds = null,
        endSeconds = null,
        pausedSeconds = pausedSeconds,
        lastPausedAt = lastPausedAt,
    )
    // endregion
    @Nested
    inner class IsPaused {
        @Test
        fun `is false when session is active and lastPausedAt is null`() {
            // ----- Arrange -----
            val session = buildSession(
                endedAt = null,
                lastPausedAt = null,
            )

            // ----- Act & Assert -----
            session.isPaused shouldBe false
        }

        @Test
        fun `is true when session is active and lastPausedAt is set`() {
            // ----- Arrange -----
            val session = buildSession(
                endedAt = null,
                lastPausedAt = Instant.parse("2024-06-01T10:30:00Z"),
            )

            // ----- Act & Assert -----
            session.isPaused shouldBe true
        }

        @Test
        fun `is false when session is ended even if lastPausedAt is set`() {
            // ----- Arrange -----
            val session = buildSession(
                endedAt = end,
                lastPausedAt = Instant.parse("2024-06-01T10:30:00Z"),
            )

            // ----- Act & Assert -----
            session.isPaused shouldBe false
        }
    }

    @Nested
    inner class ReadingDuration {
        @Test
        fun `returns full wall-clock time for a running never-paused session`() {
            // ----- Arrange -----
            val session = buildSession(
                startedAt = start,
                pausedSeconds = 0,
                lastPausedAt = null,
            )

            val now = Instant.parse("2024-06-01T10:30:00Z")

            // ----- Act -----
            val result = session.readingDuration(now = now)

            // ----- Assert -----
            result shouldBe 30.minutes
        }

        @Test
        fun `subtracts accumulated pausedSeconds from a completed session`() {
            // ----- Arrange -----
            val session = buildSession(
                startedAt = start,
                endedAt = end,
                pausedSeconds = 600,
                lastPausedAt = null,
            )

            val now = Instant.parse("2024-06-01T12:00:00Z")

            // ----- Act -----
            val result = session.readingDuration(now = now)

            // ----- Assert -----
            result shouldBe 3000.seconds
        }

        @Test
        fun `excludes the open pause duration for a currently-paused session`() {
            // ----- Arrange -----
            val pausedAt = Instant.parse("2024-06-01T10:20:00Z")

            val session = buildSession(
                startedAt = start,
                pausedSeconds = 0,
                lastPausedAt = pausedAt,
            )

            val now = Instant.parse("2024-06-01T10:50:00Z")

            // ----- Act -----
            val result = session.readingDuration(now = now)

            // ----- Assert -----
            // wall = 50 min; openPause = 30 min; reading = 20 min
            result shouldBe 20.minutes
        }

        @Test
        fun `combines pausedSeconds and open pause for a paused session with prior pauses`() {
            // ----- Arrange -----
            val pausedAt = Instant.parse("2024-06-01T10:40:00Z")

            val session = buildSession(
                startedAt = start,
                pausedSeconds = 300,
                lastPausedAt = pausedAt,
            )

            val now = Instant.parse("2024-06-01T11:00:00Z")

            // ----- Act -----
            val result = session.readingDuration(now = now)

            // ----- Assert -----
            // wall = 60 min = 3600s; accumulated = 300s; openPause = 20 min = 1200s; reading = 2100s
            result shouldBe 2100.seconds
        }

        @Test
        fun `returns zero when pausedSeconds exceeds wall-clock time`() {
            // ----- Arrange -----
            val session = buildSession(
                startedAt = start,
                endedAt = end,
                pausedSeconds = 7200,
                lastPausedAt = null,
            )

            // ----- Act -----
            val result = session.readingDuration(now = end)

            // ----- Assert -----
            result shouldBe Duration.ZERO
        }
    }
}
