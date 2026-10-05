package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.domain.model.UserBook
import nl.rhaydus.softcover.core.domain.preview.PreviewData
import nl.rhaydus.softcover.core.presentation.model.BookInitialCover
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookDetailUiStateTest {
    private fun stubEdition(id: Int = 1): BookEdition = mockk<BookEdition>().also { mock ->
        every {
            mock.id
        } returns id
    }

    private fun stubBook(
        userBook: UserBook?,
        currentEdition: BookEdition?,
        editions: List<BookEdition> = emptyList(),
    ): Book = mockk<Book>().also { mock ->
        every {
            mock.userBook
        } returns userBook
        every {
            mock.currentEdition
        } returns currentEdition
        every {
            mock.editions
        } returns editions
    }

    private fun stubUserBook(editionId: Int? = null): UserBook = mockk<UserBook>().also { mock ->
        every {
            mock.editionId
        } returns editionId
    }

    @Nested
    inner class DisplayedEdition {
        @Test
        fun `returns previewEdition when book has no user book and previewEdition is set`() {
            // ----- Arrange -----
            val preview = stubEdition(id = 5)
            val bookEdition = stubEdition(id = 10)
            val book = stubBook(
                userBook = null,
                currentEdition = bookEdition,
            )
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
            val book = stubBook(
                userBook = null,
                currentEdition = bookEdition,
            )
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
            val book = stubBook(
                userBook = userBook,
                currentEdition = bookEdition,
            )
            val state = BookDetailUiState(
                book = book,
                previewEdition = preview,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe bookEdition
        }

        @Test
        fun `returns scanned edition from book editions even when userBook has a different editionId`() {
            // ----- Arrange -----
            val scannedEdition = stubEdition(id = 42)
            val shelvedEdition = stubEdition(id = 99)
            val userBook = stubUserBook(editionId = 99)
            val book = stubBook(
                userBook = userBook,
                currentEdition = shelvedEdition,
                editions = listOf(scannedEdition, shelvedEdition),
            )
            val state = BookDetailUiState(
                book = book,
                scannedEditionId = 42,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe scannedEdition
        }

        @Test
        fun `falls back to book currentEdition when scannedEditionId is null and userBook is present`() {
            // ----- Arrange -----
            val bookEdition = stubEdition(id = 10)
            val userBook = stubUserBook(editionId = 10)
            val book = stubBook(
                userBook = userBook,
                currentEdition = bookEdition,
            )
            val state = BookDetailUiState(
                book = book,
                scannedEditionId = null,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe bookEdition
        }

        @Test
        fun `resolves scanned edition from initialCover currentEdition when editions list is empty`() {
            // ----- Arrange -----
            val coverEdition = stubEdition(id = 7)
            val initialCover = BookInitialCover(
                currentEdition = coverEdition,
                defaultEdition = null,
                fallbackCoverUrl = null,
            )
            val state = BookDetailUiState(
                book = null,
                editions = emptyList(),
                initialCover = initialCover,
                scannedEditionId = 7,
            )

            // ----- Act -----
            val result = state.displayedEdition

            // ----- Assert -----
            result shouldBe coverEdition
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
    }

    @Nested
    inner class IsEditionOwned {
        private fun listBook(editionId: Int): ListBook = ListBook(
            listBookId = editionId * 10,
            listId = 1,
            bookId = 100,
            editionId = editionId,
        )

        private fun ownedList(vararg editionIds: Int): BookList = BookList(
            id = 1,
            name = "Owned",
            slug = "owned",
            books = editionIds.map { listBook(it) },
        )

        @Test
        fun `isEditionOwned returns true when edition id is in the owned list`() {
            // ----- Arrange -----
            val edition = stubEdition(id = 77)
            val state = BookDetailUiState(
                userLists = listOf(ownedList(77)),
            )

            // ----- Act -----
            val result = state.isEditionOwned(edition)

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `isEditionOwned returns false when edition id is not in the owned list`() {
            // ----- Arrange -----
            val edition = stubEdition(id = 99)
            val state = BookDetailUiState(
                userLists = listOf(ownedList(77)),
            )

            // ----- Act -----
            val result = state.isEditionOwned(edition)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `isEditionOwned returns false when no list has slug owned`() {
            // ----- Arrange -----
            val edition = stubEdition(id = 77)
            val nonOwnedList = BookList(
                id = 2,
                name = "Favorites",
                slug = "favorites",
                books = listOf(listBook(77)),
            )
            val state = BookDetailUiState(
                userLists = listOf(nonOwnedList),
            )

            // ----- Act -----
            val result = state.isEditionOwned(edition)

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `isEditionOwned returns false when edition is null`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                userLists = listOf(ownedList(77)),
            )

            // ----- Act -----
            val result = state.isEditionOwned(null)

            // ----- Assert -----
            result shouldBe false
        }
    }

    private fun stubHeroBook(
        title: String = "Dune",
        rating: Double = 4.5,
        seriesText: String? = null,
    ): Book = mockk<Book>().also { mock ->
        every {
            mock.title
        } returns title
        every {
            mock.rating
        } returns rating
        every {
            mock.seriesText
        } returns seriesText
    }

    private fun heroInitialCover(
        title: String? = "Seed title",
        seriesText: String? = "Seed series #1",
        rating: Double? = 3.2,
    ): BookInitialCover = BookInitialCover(
        currentEdition = null,
        defaultEdition = null,
        fallbackCoverUrl = null,
        title = title,
        seriesText = seriesText,
        rating = rating,
    )

    @Nested
    inner class HeroFields {
        @Test
        fun `falls back to initialCover values when book is null`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                book = null,
                initialCover = heroInitialCover(),
            )

            // ----- Act -----
            val title = state.heroTitle
            val series = state.heroSeriesText
            val rating = state.heroRating

            // ----- Assert -----
            title shouldBe "Seed title"
            series shouldBe "Seed series #1"
            rating shouldBe 3.2
        }

        @Test
        fun `loaded book title and rating win over initialCover`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                book = stubHeroBook(
                    title = "Dune",
                    rating = 4.5,
                ),
                initialCover = heroInitialCover(),
            )

            // ----- Act -----
            val title = state.heroTitle
            val rating = state.heroRating

            // ----- Assert -----
            title shouldBe "Dune"
            rating shouldBe 4.5
        }

        @Test
        fun `loaded book without series gives empty heroSeriesText instead of initialCover value`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                book = stubHeroBook(seriesText = null),
                initialCover = heroInitialCover(seriesText = "Seed series #1"),
            )

            // ----- Act -----
            val result = state.heroSeriesText

            // ----- Assert -----
            result.shouldNotBeNull() shouldBe ""
        }

        @Test
        fun `loaded book with series gives its seriesText`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                book = stubHeroBook(seriesText = "Dune #1"),
                initialCover = heroInitialCover(),
            )

            // ----- Act -----
            val result = state.heroSeriesText

            // ----- Assert -----
            result shouldBe "Dune #1"
        }

        @Test
        fun `all hero fields are null without a book or initialCover`() {
            // ----- Arrange -----
            val state = BookDetailUiState(
                book = null,
                initialCover = null,
            )

            // ----- Act & Assert -----
            state.heroTitle.shouldBeNull()
            state.heroSeriesText.shouldBeNull()
            state.heroRating.shouldBeNull()
        }
    }
}
