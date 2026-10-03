package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShelfCoverMapperTest {
    private fun stubBook(
        id: Int,
        title: String = "Title $id",
        currentEditionId: Int? = null,
    ): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns id

        every {
            book.title
        } returns title

        every {
            book.currentEdition
        } returns currentEditionId?.let { editionId ->
            mockk<BookEdition>(relaxed = true).also { edition ->
                every {
                    edition.id
                } returns editionId
            }
        }
    }

    private fun stubEdition(
        id: Int,
        bookId: Int,
        title: String?,
    ): BookEdition = mockk<BookEdition>(relaxed = true).also { edition ->
        every {
            edition.id
        } returns id

        every {
            edition.bookId
        } returns bookId

        every {
            edition.title
        } returns title
    }

    @Nested
    inner class ToShelfBookCover {
        @Test
        fun `the shared transition key names the book`() {
            // ----- Arrange -----
            val book = stubBook(id = 12)

            // ----- Act -----
            val cover = book.toShelfBookCover()

            // ----- Assert -----
            cover.sharedTransitionKey shouldBe "book-cover-book-12"
        }

        @Test
        fun `the key does not depend on the current edition`() {
            // ----- Arrange -----
            val withEdition = stubBook(
                id = 12,
                currentEditionId = 99,
            )
            val withoutEdition = stubBook(id = 12)

            // ----- Act -----
            val keyWithEdition = withEdition.toShelfBookCover().sharedTransitionKey
            val keyWithoutEdition = withoutEdition.toShelfBookCover().sharedTransitionKey

            // ----- Assert -----
            keyWithEdition shouldBe keyWithoutEdition
        }

        @Test
        fun `the coverless title is the book title`() {
            // ----- Arrange -----
            val book = stubBook(
                id = 1,
                title = "Dune",
            )

            // ----- Act -----
            val cover = book.toShelfBookCover()

            // ----- Assert -----
            cover.coverlessTitle shouldBe "Dune"
        }
    }

    @Nested
    inner class ToShelfEditionCover {
        @Test
        fun `the shared transition key carries the book and a per-edition surface`() {
            // ----- Arrange -----
            val edition = stubEdition(
                id = 7,
                bookId = 12,
                title = "Dune",
            )

            // ----- Act -----
            val cover = edition.toShelfEditionCover()

            // ----- Assert -----
            cover.sharedTransitionKey shouldBe "book-cover-book-12-edition-7"
        }

        @Test
        fun `two editions of one book get distinct keys`() {
            // ----- Arrange -----
            val first = stubEdition(
                id = 7,
                bookId = 12,
                title = null,
            )
            val second = stubEdition(
                id = 8,
                bookId = 12,
                title = null,
            )

            // ----- Act -----
            val firstKey = first.toShelfEditionCover().sharedTransitionKey
            val secondKey = second.toShelfEditionCover().sharedTransitionKey

            // ----- Assert -----
            (firstKey == secondKey) shouldBe false
        }

        @Test
        fun `the coverless title is the edition title, empty when null`() {
            // ----- Arrange -----
            val titled = stubEdition(
                id = 7,
                bookId = 12,
                title = "Dune",
            )
            val untitled = stubEdition(
                id = 8,
                bookId = 12,
                title = null,
            )

            // ----- Act -----
            val titledCover = titled.toShelfEditionCover()
            val untitledCover = untitled.toShelfEditionCover()

            // ----- Assert -----
            titledCover.coverlessTitle shouldBe "Dune"
            untitledCover.coverlessTitle shouldBe ""
        }
    }
}
