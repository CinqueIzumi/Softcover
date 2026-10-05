package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.BookEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ShelfCoverMapperTest {
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
