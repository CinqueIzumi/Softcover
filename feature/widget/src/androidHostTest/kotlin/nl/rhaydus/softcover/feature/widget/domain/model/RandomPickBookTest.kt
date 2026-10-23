package nl.rhaydus.softcover.feature.widget.domain.model

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RandomPickBookTest {
    private val userEdition = edition(
        id = 7,
        title = "User edition title",
        authorName = "User edition author",
    )

    private val book = Book(
        id = 1,
        title = "Book title",
        editions = listOf(userEdition),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2000,
        coverUrl = "",
        authors = listOf(
            Author(
                id = 1,
                name = "Book author",
            ),
        ),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = mockk<UserBook> {
            every {
                editionId
            } returns 7
            every {
                status
            } returns BookStatus.WantToRead
        },
    )

    private fun edition(
        id: Int = 50,
        title: String?,
        authorName: String?,
    ): BookEdition = BookEdition(
        id = id,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = title,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = null,
        audioSeconds = null,
        authors = listOfNotNull(
            authorName?.let {
                Author(
                    id = id,
                    name = it,
                )
            },
        ),
        releaseYear = 2000,
        format = "",
        owned = false,
    )

    @Nested
    inner class Title {
        @Test
        fun `a list edition's title wins`() {
            // ----- Arrange -----
            val pick = RandomPickBook(
                book = book,
                listEdition = edition(
                    title = "List edition title",
                    authorName = null,
                ),
            )

            // ----- Act -----
            val result = pick.title

            // ----- Assert -----
            result shouldBe "List edition title"
        }

        @Test
        fun `a blank list edition title falls back to the book title, not the user edition`() {
            // ----- Arrange -----
            val pick = RandomPickBook(
                book = book,
                listEdition = edition(
                    title = " ",
                    authorName = null,
                ),
            )

            // ----- Act -----
            val result = pick.title

            // ----- Assert -----
            result shouldBe "Book title"
        }

        @Test
        fun `no list edition shows the book's display title`() {
            // ----- Arrange -----
            val pick = RandomPickBook(
                book = book,
                listEdition = null,
            )

            // ----- Act -----
            val result = pick.title

            // ----- Assert -----
            result shouldBe "User edition title"
        }
    }

    @Nested
    inner class AuthorLine {
        @Test
        fun `a list edition's authors win`() {
            // ----- Arrange -----
            val pick = RandomPickBook(
                book = book,
                listEdition = edition(
                    title = null,
                    authorName = "List edition author",
                ),
            )

            // ----- Act -----
            val result = pick.author

            // ----- Assert -----
            result shouldBe "List edition author"
        }

        @Test
        fun `a list edition without authors falls back to the book authors`() {
            // ----- Arrange -----
            val pick = RandomPickBook(
                book = book,
                listEdition = edition(
                    title = null,
                    authorName = null,
                ),
            )

            // ----- Act -----
            val result = pick.author

            // ----- Assert -----
            result shouldBe "Book author"
        }

        @Test
        fun `no list edition shows the book's author string`() {
            // ----- Arrange -----
            val pick = RandomPickBook(
                book = book,
                listEdition = null,
            )

            // ----- Act -----
            val result = pick.author

            // ----- Assert -----
            result shouldBe "User edition author"
        }
    }
}
