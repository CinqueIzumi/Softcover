package nl.rhaydus.softcover.core.domain.model

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookEditionTest {
    private fun buildEdition(
        readingFormat: ReadingFormat? = null,
        audioSeconds: Int? = null,
    ) = BookEdition(
        id = 1,
        canonicalId = null,
        bookId = 10,
        publisher = null,
        title = "Test Title",
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = audioSeconds,
        authors = emptyList(),
        releaseYear = 2024,
        releaseDate = LocalDate(
            2024,
            1,
            1,
        ),
        format = "Audiobook",
        readingFormat = readingFormat,
        owned = false,
    )

    @Nested
    inner class IsAudiobook {
        @Test
        fun `is true when readingFormat is Audio`() {
            // ----- Arrange -----
            val edition = buildEdition(readingFormat = ReadingFormat.Audio)

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe true
        }

        @Test
        fun `is false when readingFormat is Physical`() {
            // ----- Arrange -----
            val edition = buildEdition(readingFormat = ReadingFormat.Physical)

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe false
        }

        @Test
        fun `is false when readingFormat is Both`() {
            // ----- Arrange -----
            val edition = buildEdition(readingFormat = ReadingFormat.Both)

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe false
        }

        @Test
        fun `is false when readingFormat is Ebook`() {
            // ----- Arrange -----
            val edition = buildEdition(readingFormat = ReadingFormat.Ebook)

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe false
        }

        @Test
        fun `is false when readingFormat is null`() {
            // ----- Arrange -----
            val edition = buildEdition(readingFormat = null)

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe false
        }

        @Test
        fun `is false when audioSeconds is non-null but readingFormat is not Audio`() {
            // ----- Arrange -----
            val edition = buildEdition(
                readingFormat = ReadingFormat.Physical,
                audioSeconds = 3600,
            )

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe false
        }

        @Test
        fun `is true when readingFormat is Audio even when audioSeconds is null`() {
            // ----- Arrange -----
            val edition = buildEdition(
                readingFormat = ReadingFormat.Audio,
                audioSeconds = null,
            )

            // ----- Act & Assert -----
            edition.isAudiobook shouldBe true
        }
    }
}
