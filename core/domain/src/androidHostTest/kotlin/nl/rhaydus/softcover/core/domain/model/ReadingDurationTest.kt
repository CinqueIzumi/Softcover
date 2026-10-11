package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class ReadingDurationTest {
    @Test
    fun `running session with no pauses returns the wall time`() {
        // ----- Arrange -----
        val startedAt = 1_000L
        val end = 61_000L

        // ----- Act -----
        val result = readingDuration(
            startedAtEpochMs = startedAt,
            pausedSeconds = 0,
            lastPausedAtEpochMs = null,
            endEpochMs = end,
        )

        // ----- Assert -----
        result shouldBe 60.seconds
    }

    @Test
    fun `closed pauses are subtracted from the wall time`() {
        // ----- Arrange -----
        val end = 100_000L

        // ----- Act -----
        val result = readingDuration(
            startedAtEpochMs = 0L,
            pausedSeconds = 30,
            lastPausedAtEpochMs = null,
            endEpochMs = end,
        )

        // ----- Assert -----
        result shouldBe 70.seconds
    }

    @Test
    fun `open pause with fractional instants floors wall and open pause separately`() {
        // ----- Arrange -----
        val end = 10_200L
        val openPauseStart = 4_700L

        // ----- Act -----
        val result = readingDuration(
            startedAtEpochMs = 0L,
            pausedSeconds = 0,
            lastPausedAtEpochMs = openPauseStart,
            endEpochMs = end,
        )

        // ----- Assert -----
        result shouldBe 5.seconds
    }

    @Test
    fun `closed and open pauses are both subtracted`() {
        // ----- Arrange -----
        val end = 100_000L
        val openPauseStart = 90_000L

        // ----- Act -----
        val result = readingDuration(
            startedAtEpochMs = 0L,
            pausedSeconds = 20,
            lastPausedAtEpochMs = openPauseStart,
            endEpochMs = end,
        )

        // ----- Assert -----
        result shouldBe 70.seconds
    }

    @Test
    fun `result is clamped at zero when pauses exceed the wall time`() {
        // ----- Arrange -----
        val end = 10_000L

        // ----- Act -----
        val result = readingDuration(
            startedAtEpochMs = 0L,
            pausedSeconds = 60,
            lastPausedAtEpochMs = null,
            endEpochMs = end,
        )

        // ----- Assert -----
        result shouldBe 0.seconds
    }

    @Test
    fun `session readingDuration equals the top-level function for the same fractional inputs`() {
        // ----- Arrange -----
        val startedAt = Instant.fromEpochMilliseconds(500L)
        val openPauseStart = Instant.fromEpochMilliseconds(5_200L)
        val now = Instant.fromEpochMilliseconds(10_700L)
        val session = ReadingSession(
            id = 1L,
            bookId = 10,
            startedAt = startedAt,
            endedAt = null,
            startPage = null,
            endPage = null,
            startSeconds = null,
            endSeconds = null,
            pausedSeconds = 2,
            lastPausedAt = openPauseStart,
        )

        // ----- Act -----
        val result = session.readingDuration(now = now)

        // ----- Assert -----
        result shouldBe readingDuration(
            startedAtEpochMs = 500L,
            pausedSeconds = 2,
            lastPausedAtEpochMs = 5_200L,
            endEpochMs = 10_700L,
        )
    }
}
