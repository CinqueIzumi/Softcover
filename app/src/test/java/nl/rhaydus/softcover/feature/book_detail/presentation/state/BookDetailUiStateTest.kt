package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.PreviewData
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDetailUiStateTest {

    private fun stubEdition(id: Int = 1): BookEdition = mockk<BookEdition>().also { mock ->
        every { mock.id } returns id
    }

    private fun stubBook(userBook: UserBook?, currentEdition: BookEdition?): Book =
        mockk<Book>().also { mock ->
            every { mock.userBook } returns userBook
            every { mock.currentEdition } returns currentEdition
        }

    @Nested
    inner class DisplayedEdition {

        @Test
        fun `returns previewEdition when book has no user book and previewEdition is set`() {
            // ----- Arrange -----
            val preview = stubEdition(id = 5)
            val bookEdition = stubEdition(id = 10)
            val book = stubBook(userBook = null, currentEdition = bookEdition)
            val state = BookDetailUiState(
                book = book,
                previewEdition = preview,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe preview
        }

        @Test
        fun `falls back to book currentEdition when off-shelf and no preview`() {
            // ----- Arrange -----
            val bookEdition = stubEdition(id = 10)
            val book = stubBook(userBook = null, currentEdition = bookEdition)
            val state = BookDetailUiState(
                book = book,
                previewEdition = null,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe bookEdition
        }

        @Test
        fun `falls back to initialCover currentEdition when off-shelf, no preview, and no book`() {
            // ----- Arrange -----
            val coverEdition = stubEdition(id = 20)
            val initialCover = BookInitialCover(
                currentEdition = coverEdition,
                defaultEdition = null,
                fallbackCoverUrl = null,
            )
            val state = BookDetailUiState(
                book = null,
                previewEdition = null,
                initialCover = initialCover,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe coverEdition
        }

        @Test
        fun `ignores previewEdition and returns book currentEdition when user book is present`() {
            // ----- Arrange -----
            val preview = stubEdition(id = 5)
            val bookEdition = stubEdition(id = 10)
            val userBook = mockk<UserBook>()
            val book = stubBook(userBook = userBook, currentEdition = bookEdition)
            val state = BookDetailUiState(
                book = book,
                previewEdition = preview,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe bookEdition
        }
    }

    @Nested
    inner class FilteredEditions {

        private val editionWithIsbn = PreviewData.baseEdition.copy(
            id = 1,
            isbn10 = "0385333498",
            publisher = "Doubleday",
        )

        private val editionWithoutIsbn = PreviewData.baseEdition.copy(
            id = 2,
            isbn10 = null,
            publisher = "Penguin Random House",
        )

        private val editionNullBoth = PreviewData.baseEdition.copy(
            id = 3,
            isbn10 = null,
            publisher = null,
        )

        @Test
        fun `returns all editions when query is empty`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionWithIsbn, editionWithoutIsbn),
                editionSearchQuery = "",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionWithIsbn, editionWithoutIsbn)
        }

        @Test
        fun `returns all editions when query is blank whitespace only`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionWithIsbn, editionWithoutIsbn),
                editionSearchQuery = "   ",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionWithIsbn, editionWithoutIsbn)
        }

        @Test
        fun `returns all editions when query is tab and newline whitespace`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionWithIsbn, editionWithoutIsbn),
                editionSearchQuery = "\t\n",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionWithIsbn, editionWithoutIsbn)
        }

        @Test
        fun `filters by isbn10 substring match`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionWithIsbn, editionWithoutIsbn),
                editionSearchQuery = "03853",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionWithIsbn)
        }

        @Test
        fun `filters by isbn13 substring match`() {
            // ----- Arrange -----
            val editionWithIsbn13 = PreviewData.baseEdition.copy(
                id = 10,
                isbn13 = "9780451524935",
                isbn10 = null,
            )
            val state = BookDetailUiState(
                editions = listOf(editionWithIsbn13),
                editionSearchQuery = "97804515",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionWithIsbn13)
        }

        @Test
        fun `isbn10 match is case-insensitive`() {
            // ----- Arrange -----
            val editionLowerIsbn = PreviewData.baseEdition.copy(
                id = 10,
                isbn10 = "abcdef",
                publisher = null,
            )
            val state = BookDetailUiState(
                editions = listOf(editionLowerIsbn),
                editionSearchQuery = "ABCDEF",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldHaveSize 1
        }

        @Test
        fun `filters by publisher substring match`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionWithIsbn, editionWithoutIsbn),
                editionSearchQuery = "Penguin",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionWithoutIsbn)
        }

        @Test
        fun `publisher match is case-insensitive`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionWithIsbn, editionWithoutIsbn),
                editionSearchQuery = "penguin random house",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionWithoutIsbn)
        }

        @Test
        fun `returns editions matching either isbn10 or publisher`() {
            // ----- Arrange -----
            val editionIsbnMatch = PreviewData.baseEdition.copy(
                id = 20,
                isbn10 = "1234567890",
                publisher = "Other Press",
            )
            val editionPublisherMatch = PreviewData.baseEdition.copy(
                id = 21,
                isbn10 = "0000000000",
                publisher = "Publisher With 1234 In Name",
            )
            val editionNoMatch = PreviewData.baseEdition.copy(
                id = 22,
                isbn10 = "9999999999",
                publisher = "Unrelated",
            )
            val state = BookDetailUiState(
                editions = listOf(editionIsbnMatch, editionPublisherMatch, editionNoMatch),
                editionSearchQuery = "1234",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result shouldContainExactly listOf(editionIsbnMatch, editionPublisherMatch)
        }

        @Test
        fun `returns empty list when no edition matches`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionWithIsbn, editionWithoutIsbn),
                editionSearchQuery = "zzznomatch",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `null isbn10 does not crash and is excluded from match`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionWithoutIsbn),
                editionSearchQuery = "0385333498",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `null publisher does not crash and is excluded from match`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionWithIsbn),
                editionSearchQuery = "Penguin",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `edition with both isbn10 and publisher null does not crash and is excluded`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = listOf(editionNullBoth),
                editionSearchQuery = "anything",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `returns empty list when editions list is empty`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                editions = emptyList(),
                editionSearchQuery = "978",
            )

            // ----- Act -----
            val result = state.filteredEditions

            // ----- Assert -----
            result.shouldBeEmpty()
        }
    }
}
