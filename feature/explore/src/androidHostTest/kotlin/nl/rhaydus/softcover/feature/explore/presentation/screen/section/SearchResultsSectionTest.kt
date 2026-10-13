package nl.rhaydus.softcover.feature.explore.presentation.screen.section

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SearchResultsSectionTest {
    private fun loadedCard(bookId: Int): BookCardUiModel =
        BookCardUiModel.previews.values
            .first()
            .copy(
                key = BookCardKey.Book(bookId = bookId),
                isLoading = false,
            )

    private fun loadingCard(bookId: Int): BookCardUiModel = loadedCard(bookId = bookId).copy(isLoading = true)

    private fun stateWith(
        cards: List<BookCardUiModel>,
        loadingMore: Boolean = false,
    ): ExploreScreenUiState =
        ExploreScreenUiState(
            searchResults = BookCardSet.standIns(persistentListOf(*cards.toTypedArray())),
            loadingMoreQueriedBooks = loadingMore,
        )

    @Nested
    inner class ShowsSearchStandIns {
        @Test
        fun `a loading card shows stand-ins`() {
            // ----- Arrange -----
            val state = stateWith(cards = listOf(loadingCard(bookId = 1)))

            // ----- Act -----
            val result = state.showsSearchStandIns

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `one loading card among loaded cards shows stand-ins`() {
            // ----- Arrange -----
            val state = stateWith(
                cards = listOf(
                    loadedCard(bookId = 1),
                    loadingCard(bookId = 2),
                ),
            )

            // ----- Act -----
            val result = state.showsSearchStandIns

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `only loaded cards show no stand-ins`() {
            // ----- Arrange -----
            val state = stateWith(
                cards = listOf(
                    loadedCard(bookId = 1),
                    loadedCard(bookId = 2),
                ),
            )

            // ----- Act -----
            val result = state.showsSearchStandIns

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `an empty result list shows no stand-ins`() {
            // ----- Arrange -----
            val state = stateWith(cards = emptyList())

            // ----- Act -----
            val result = state.showsSearchStandIns

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class ShowsLoadingMoreIndicator {
        @Test
        fun `loading more with loaded cards shows the indicator`() {
            // ----- Arrange -----
            val state = stateWith(
                cards = listOf(loadedCard(bookId = 1)),
                loadingMore = true,
            )

            // ----- Act -----
            val result = state.showsLoadingMoreIndicator

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `loading more while stand-ins are shown hides the indicator`() {
            // ----- Arrange -----
            val state = stateWith(
                cards = listOf(loadingCard(bookId = 1)),
                loadingMore = true,
            )

            // ----- Act -----
            val result = state.showsLoadingMoreIndicator

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `not loading more hides the indicator`() {
            // ----- Arrange -----
            val state = stateWith(
                cards = listOf(loadedCard(bookId = 1)),
                loadingMore = false,
            )

            // ----- Act -----
            val result = state.showsLoadingMoreIndicator

            // ----- Assert -----
            result shouldBe false
        }
    }
}
