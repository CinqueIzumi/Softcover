package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

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
            val session = buildSession(endedAt = null, lastPausedAt = null)

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

        @Test
        fun `is false when session is ended and lastPausedAt is null`() {
            // ----- Arrange -----
            val session = buildSession(endedAt = end, lastPausedAt = null)

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
            result shouldBe Duration.ofMinutes(30)
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
            result shouldBe Duration.ofSeconds(3000)
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
            result shouldBe Duration.ofMinutes(20)
        }

        @Test
        fun `readingDuration stays constant as now advances during an open pause`() {
            // ----- Arrange -----
            val pausedAt = Instant.parse("2024-06-01T10:20:00Z")

            val session = buildSession(
                startedAt = start,
                pausedSeconds = 0,
                lastPausedAt = pausedAt,
            )

            val now1 = Instant.parse("2024-06-01T10:50:00Z")
            val now2 = Instant.parse("2024-06-01T11:10:00Z")

            // ----- Act -----
            val result1 = session.readingDuration(now = now1)
            val result2 = session.readingDuration(now = now2)

            // ----- Assert -----
            result1 shouldBe result2
        }

        @Test
        fun `uses endedAt instead of now for a completed session`() {
            // ----- Arrange -----
            val session = buildSession(
                startedAt = start,
                endedAt = end,
                pausedSeconds = 0,
                lastPausedAt = null,
            )

            val laterNow = Instant.parse("2024-06-01T15:00:00Z")

            // ----- Act -----
            val result = session.readingDuration(now = laterNow)

            // ----- Assert -----
            result shouldBe Duration.ofHours(1)
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
            result shouldBe Duration.ofSeconds(2100)
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
