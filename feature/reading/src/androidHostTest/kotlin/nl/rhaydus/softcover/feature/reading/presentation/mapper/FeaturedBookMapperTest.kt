package nl.rhaydus.softcover.feature.reading.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class FeaturedBookMapperTest {
    private fun edition(
        authors: List<Author> = emptyList(),
        readingFormat: ReadingFormat? = null,
    ): BookEdition = BookEdition(
        id = 50,
        canonicalId = null,
        bookId = 7,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = authors,
        releaseYear = 2020,
        format = "Paperback",
        readingFormat = readingFormat,
        owned = false,
    )

    private fun book(
        defaultEdition: BookEdition? = null,
        bookSeries: BookSeries? = null,
        positionsInSeries: List<Double> = emptyList(),
    ): Book = Book(
        id = 7,
        title = "Some Title",
        editions = emptyList(),
        defaultEdition = defaultEdition,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = bookSeries,
        positionsInSeries = positionsInSeries,
        isCompilation = false,
        userBook = null,
        userBookRead = null,
    )

    @Nested
    inner class PassThrough {
        @Test
        fun `carries the id and title across`() {
            // ----- Arrange -----
            val subject = book()

            // ----- Act -----
            val result = subject.toFeaturedBookUiModel()

            // ----- Assert -----
            result.id shouldBe 7
            result.title shouldBe "Some Title"
        }

        @Test
        fun `carries the series text across`() {
            // ----- Arrange -----
            val subject = book(bookSeries = BookSeries(
                id = 1,
                name = "The Saga",
                amountOfBooks = 3,
            ),)

            // ----- Act -----
            val result = subject.toFeaturedBookUiModel()

            // ----- Assert -----
            result.seriesText shouldBe subject.seriesText
        }

        @Test
        fun `has no series text for a standalone book`() {
            // ----- Arrange -----
            val subject = book(bookSeries = null)

            // ----- Act -----
            val result = subject.toFeaturedBookUiModel()

            // ----- Assert -----
            result.seriesText.shouldBeNull()
        }
    }

    @Nested
    inner class Authors {
        @Test
        fun `joins the current edition authors`() {
            // ----- Arrange -----
            val subject = book(
                defaultEdition = edition(authors = listOf(Author(
                    1,
                    "Ann",
                ), Author(
                    2,
                    "Bob",
                ),),),
            )

            // ----- Act -----
            val result = subject.toFeaturedBookUiModel()

            // ----- Assert -----
            result.authors shouldBe "Ann, Bob"
        }

        @Test
        fun `is null without a current edition`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = null)

            // ----- Act -----
            val result = subject.toFeaturedBookUiModel()

            // ----- Assert -----
            result.authors.shouldBeNull()
        }
    }

    @Nested
    inner class IsAudiobook {
        @Test
        fun `is true for an audio edition`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = edition(readingFormat = ReadingFormat.Audio))

            // ----- Act -----
            val result = subject.toFeaturedBookUiModel()

            // ----- Assert -----
            result.isAudiobook shouldBe true
        }

        @Test
        fun `is false for a physical edition`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = edition(readingFormat = ReadingFormat.Physical))

            // ----- Act -----
            val result = subject.toFeaturedBookUiModel()

            // ----- Assert -----
            result.isAudiobook shouldBe false
        }

        @Test
        fun `is false without a current edition`() {
            // ----- Arrange -----
            val subject = book(defaultEdition = null)

            // ----- Act -----
            val result = subject.toFeaturedBookUiModel()

            // ----- Assert -----
            result.isAudiobook shouldBe false
        }
    }
}
