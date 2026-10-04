package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookShareCardMapperTest {
    private fun stubEdition(
        localImagePath: String? = null,
        url: String? = null,
        releaseYear: Int = 2021,
        pages: Int? = 320,
    ): BookEdition = mockk {
        every {
            this@mockk.localImagePath
        } returns localImagePath

        every {
            this@mockk.url
        } returns url

        every {
            this@mockk.releaseYear
        } returns releaseYear

        every {
            this@mockk.pages
        } returns pages
    }

    private fun stubUserBook(rating: Double?): UserBook = mockk {
        every {
            this@mockk.rating
        } returns rating
    }

    private fun stubBook(
        title: String = "Test Book",
        authors: List<Author> = listOf(
            Author(
                id = 1,
                name = "Jane Austen",
            ),
        ),
        rating: Double = 4.0,
        description: String = "A description",
        releaseYear: Int = 2020,
        coverUrl: String = "https://book.cover/img.jpg",
        defaultEdition: BookEdition? = null,
        userBook: UserBook? = null,
    ): Book = mockk {
        every {
            this@mockk.title
        } returns title

        every {
            this@mockk.authors
        } returns authors

        every {
            this@mockk.rating
        } returns rating

        every {
            this@mockk.description
        } returns description

        every {
            this@mockk.releaseYear
        } returns releaseYear

        every {
            this@mockk.coverUrl
        } returns coverUrl

        every {
            this@mockk.defaultEdition
        } returns defaultEdition

        every {
            this@mockk.userBook
        } returns userBook
    }

    @Nested
    inner class UserRating {
        @Test
        fun `doubles the star rating into the verdict score`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
                userBook = stubUserBook(rating = 4.5),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.statsLine shouldBe "Your 9/10"
        }

        @Test
        fun `is left out when the rating is zero`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
                userBook = stubUserBook(rating = 0.0),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.statsLine shouldBe null
        }

        @Test
        fun `is left out when the rating is null`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
                userBook = stubUserBook(rating = null),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.statsLine shouldBe null
        }

        @Test
        fun `is left out when the book is not on a shelf`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
                userBook = null,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.statsLine shouldBe null
        }
    }

    @Nested
    inner class CommunityRating {
        @Test
        fun `rounds the community rating to one decimal`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 4.25,
                releaseYear = -1,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.statsLine shouldBe "★ 4.3"
        }

        @Test
        fun `is left out when the community rating is zero`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.statsLine shouldBe null
        }
    }

    @Nested
    inner class StatsLine {
        @Test
        fun `joins every stat in reading order`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 4.0,
                userBook = stubUserBook(rating = 3.0),
            )
            val edition = stubEdition(
                releaseYear = 2019,
                pages = 640,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = edition)

            // ----- Assert -----
            result.statsLine shouldBe "★ 4.0 · Your 6/10 · 2019 · 640 pages"
        }

        @Test
        fun `leaves out a non-positive page count`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
            )
            val edition = stubEdition(
                releaseYear = -1,
                pages = 0,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = edition)

            // ----- Assert -----
            result.statsLine shouldBe null
        }
    }

    @Nested
    inner class Identity {
        @Test
        fun `carries the title and the first author`() {
            // ----- Arrange -----
            val book = stubBook(
                title = "Emma",
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Jane Austen",
                    ),
                    Author(
                        id = 2,
                        name = "Someone Else",
                    ),
                ),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.title shouldBe "Emma"
            result.author shouldBe "Jane Austen"
        }

        @Test
        fun `author is empty when the book has no authors`() {
            // ----- Arrange -----
            val book = stubBook(authors = emptyList())

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.author shouldBe ""
        }

        @Test
        fun `description is null when blank`() {
            // ----- Arrange -----
            val book = stubBook(description = "  ")

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.description shouldBe null
        }
    }

    @Nested
    inner class EditionResolution {
        @Test
        fun `prefers the given edition over the default edition`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                defaultEdition = stubEdition(pages = 100),
            )
            val edition = stubEdition(
                pages = 640,
                releaseYear = 2019,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = edition)

            // ----- Assert -----
            result.statsLine shouldBe "2019 · 640 pages"
        }

        @Test
        fun `falls back to the default edition when none is given`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                defaultEdition = stubEdition(pages = 100),
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = null)

            // ----- Assert -----
            result.statsLine shouldBe "2021 · 100 pages"
        }

        @Test
        fun `the release year falls back to the book when the edition has the unknown sentinel`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = 2020,
            )
            val edition = stubEdition(
                releaseYear = -1,
                pages = null,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = edition)

            // ----- Assert -----
            result.statsLine shouldBe "2020"
        }

        @Test
        fun `the year is left out when both the edition and the book have the unknown sentinel`() {
            // ----- Arrange -----
            val book = stubBook(
                rating = 0.0,
                releaseYear = -1,
            )
            val edition = stubEdition(
                releaseYear = -1,
                pages = null,
            )

            // ----- Act -----
            val result = book.toBookShareCardUiModel(edition = edition)

            // ----- Assert -----
            result.statsLine shouldBe null
        }

        @Test
        fun `cover prefers the local image path, then the edition url, then the book cover`() {
            // ----- Arrange -----
            val book = stubBook(coverUrl = "book-cover")
            val local = stubEdition(
                localImagePath = "local",
                url = "edition-url",
            )
            val remote = stubEdition(url = "edition-url")
            val bare = stubEdition()

            // ----- Act & Assert -----
            book.toBookShareCardUiModel(edition = local).coverUrl shouldBe "local"
            book.toBookShareCardUiModel(edition = remote).coverUrl shouldBe "edition-url"
            book.toBookShareCardUiModel(edition = bare).coverUrl shouldBe "book-cover"
        }
    }
}
