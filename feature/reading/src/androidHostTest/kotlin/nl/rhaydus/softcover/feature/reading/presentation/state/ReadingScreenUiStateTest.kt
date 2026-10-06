package nl.rhaydus.softcover.feature.reading.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.presentation.mapper.FEATURED_STAND_IN_BOOK
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingScreenUiStateTest {
    private fun book(id: Int): Book = FEATURED_STAND_IN_BOOK.copy(
        id = id,
        title = "Title $id",
    )

    @Nested
    inner class FeaturedBook {
        @Test
        fun `is the loading stand-in while there are no books`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(books = emptyList())

            // ----- Act & Assert -----
            state.featuredBook shouldBe FEATURED_STAND_IN_BOOK
        }

        @Test
        fun `is the first book when there are books`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(books = listOf(book(5), book(6)))

            // ----- Act & Assert -----
            state.featuredBook.id shouldBe 5
        }
    }

    @Nested
    inner class IsLoadingFeatured {
        @Test
        fun `is true while there are no books`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(books = emptyList())

            // ----- Act & Assert -----
            state.isLoadingFeatured shouldBe true
        }

        @Test
        fun `is false once there is a book`() {
            // ----- Arrange -----
            val state = ReadingScreenUiState(books = listOf(book(1)))

            // ----- Act & Assert -----
            state.isLoadingFeatured shouldBe false
        }
    }
}
