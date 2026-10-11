package nl.rhaydus.softcover.orchestration.session

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingSession
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.time.Instant

class ActiveSessionUiMapperTest {
    private fun book(authors: List<Author>): Book = mockk<Book> {
        every {
            id
        } returns 42
        every {
            title
        } returns "Piranesi"
        every {
            this@mockk.authors
        } returns authors
        every {
            currentEdition
        } returns null
        every {
            defaultEdition
        } returns null
        every {
            coverUrl
        } returns ""
    }

    private fun session(
        startedAtMs: Long = 1_000_000L,
        pausedSeconds: Int = 0,
        lastPausedAtMs: Long? = null,
    ): ReadingSession = ReadingSession(
        id = 1L,
        bookId = 42,
        startedAt = Instant.fromEpochMilliseconds(startedAtMs),
        endedAt = null,
        startPage = null,
        endPage = null,
        startSeconds = null,
        endSeconds = null,
        pausedSeconds = pausedSeconds,
        lastPausedAt = lastPausedAtMs?.let { Instant.fromEpochMilliseconds(it) },
    )

    @Nested
    inner class Identity {
        @Test
        fun `uses the book title and the first author name`() {
            // ----- Arrange -----
            val book = book(listOf(Author(
                1,
                "Susanna Clarke",
            ), Author(
                2,
                "Someone Else",
            ),),)

            // ----- Act -----
            val result = toActiveSessionUiModel(
                session(),
                book,
            )

            // ----- Assert -----
            result.title shouldBe "Piranesi"
            result.author shouldBe "Susanna Clarke"
        }

        @Test
        fun `author is null when the book has no authors`() {
            // ----- Arrange -----
            val book = book(emptyList())

            // ----- Act -----
            val result = toActiveSessionUiModel(
                session(),
                book,
            )

            // ----- Assert -----
            result.author shouldBe null
        }
    }

    @Nested
    inner class Timing {
        @Test
        fun `running session has no pausedAt and starts at startedAt when never paused`() {
            // ----- Arrange -----
            val session = session(startedAtMs = 5_000L)

            // ----- Act -----
            val result = toActiveSessionUiModel(
                session,
                book(emptyList()),
            )

            // ----- Assert -----
            result.isPaused shouldBe false
            result.startedAtEpochMs shouldBe 5_000L
            result.pausedSeconds shouldBe 0
            result.pausedAtEpochMs shouldBe null
        }

        @Test
        fun `closed pauses are exposed as pausedSeconds`() {
            // ----- Arrange -----
            val session = session(
                startedAtMs = 5_000L,
                pausedSeconds = 30,
            )

            // ----- Act -----
            val result = toActiveSessionUiModel(
                session,
                book(emptyList()),
            )

            // ----- Assert -----
            result.startedAtEpochMs shouldBe 5_000L
            result.pausedSeconds shouldBe 30
            result.pausedAtEpochMs shouldBe null
        }

        @Test
        fun `paused session exposes the pause instant and the start`() {
            // ----- Arrange -----
            val session = session(
                startedAtMs = 5_000L,
                pausedSeconds = 10,
                lastPausedAtMs = 99_000L,
            )

            // ----- Act -----
            val result = toActiveSessionUiModel(
                session,
                book(emptyList()),
            )

            // ----- Assert -----
            result.isPaused shouldBe true
            result.startedAtEpochMs shouldBe 5_000L
            result.pausedSeconds shouldBe 10
            result.pausedAtEpochMs shouldBe 99_000L
        }
    }

    @Nested
    inner class Covers {
        @Test
        fun `peek bar uses the flat cover and focus uses the spotlight cover`() {
            // ----- Arrange -----
            val book = book(emptyList())

            // ----- Act -----
            val result = toActiveSessionUiModel(
                session(),
                book,
            )

            // ----- Assert -----
            result.peekBarCover.variant shouldBe CoverVariant.Flat
            result.focusCover.variant shouldBe CoverVariant.Spotlight
        }
    }
}
