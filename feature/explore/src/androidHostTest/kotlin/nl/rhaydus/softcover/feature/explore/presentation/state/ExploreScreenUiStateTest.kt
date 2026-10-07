package nl.rhaydus.softcover.feature.explore.presentation.state

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExploreScreenUiStateTest {
    private val mood = MoodTag(
        id = 1,
        label = "Cozy",
        slug = "cozy",
        bookCount = 10,
    )

    @Nested
    inner class SearchPhase {
        @Test
        fun `unfocused empty query with no mood filter is FEED`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState()

            // ----- Act -----
            val result = state.searchPhase

            // ----- Assert -----
            result shouldBe ExploreSearchPhase.FEED
        }

        @Test
        fun `focused empty query with no mood filter is FOCUS`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(searchFocused = true)

            // ----- Act -----
            val result = state.searchPhase

            // ----- Assert -----
            result shouldBe ExploreSearchPhase.FOCUS
        }

        @Test
        fun `non-empty searchText is RESULTS regardless of focus`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                searchText = "dune",
                searchFocused = true,
            )

            // ----- Act -----
            val result = state.searchPhase

            // ----- Assert -----
            result shouldBe ExploreSearchPhase.RESULTS
        }

        @Test
        fun `active mood filter is RESULTS even when unfocused`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                activeMoodFilter = mood,
                searchFocused = false,
            )

            // ----- Act -----
            val result = state.searchPhase

            // ----- Assert -----
            result shouldBe ExploreSearchPhase.RESULTS
        }

        @Test
        fun `isLoading is LOADING even with a non-empty searchText`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                isLoading = true,
                searchText = "dune",
            )

            // ----- Act -----
            val result = state.searchPhase

            // ----- Assert -----
            result shouldBe ExploreSearchPhase.LOADING
        }

        @Test
        fun `isLoading is LOADING even with an active mood filter`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                isLoading = true,
                activeMoodFilter = mood,
            )

            // ----- Act -----
            val result = state.searchPhase

            // ----- Assert -----
            result shouldBe ExploreSearchPhase.LOADING
        }
    }

    @Nested
    inner class HasActiveSearch {
        @Test
        fun `focused empty query with no mood filter is false`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(searchFocused = true)

            // ----- Act -----
            val result = state.hasActiveSearch

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `non-empty searchText is true`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(searchText = "dune")

            // ----- Act -----
            val result = state.hasActiveSearch

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `active mood filter with empty searchText is true`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(activeMoodFilter = mood)

            // ----- Act -----
            val result = state.hasActiveSearch

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `isLoading is true`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(isLoading = true)

            // ----- Act -----
            val result = state.hasActiveSearch

            // ----- Assert -----
            result shouldBe true
        }
    }

    @Nested
    inner class ShowsNoSearchResults {
        @Test
        fun `finished text search with no books and no cards is true`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                searchText = "zzzz",
                searchResults = BookCardSet.empty(),
            )

            // ----- Act -----
            val result = state.showsNoSearchResults

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `finished mood search with no books and no cards is true`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                activeMoodFilter = mood,
                searchResults = BookCardSet.empty(),
            )

            // ----- Act -----
            val result = state.showsNoSearchResults

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `loading search is false`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                searchText = "zzzz",
                isLoading = true,
                searchResults = BookCardSet.empty(),
            )

            // ----- Act -----
            val result = state.showsNoSearchResults

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `search error is false`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                searchText = "zzzz",
                searchError = "offline",
                searchResults = BookCardSet.empty(),
            )

            // ----- Act -----
            val result = state.showsNoSearchResults

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `search with result cards is false`() {
            // ----- Arrange -----
            val card = BookCardUiModel.previews.values.first()
            val state = ExploreScreenUiState(
                searchText = "dune",
                searchResults = BookCardSet.standIns(persistentListOf(card)),
            )

            // ----- Act -----
            val result = state.showsNoSearchResults

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `search with queried books is false`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                searchText = "dune",
                queriedBooks = listOf(mockk<Book>()),
                searchResults = BookCardSet.empty(),
            )

            // ----- Act -----
            val result = state.showsNoSearchResults

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `no active search is false even with no results`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(searchResults = BookCardSet.empty())

            // ----- Act -----
            val result = state.showsNoSearchResults

            // ----- Assert -----
            result shouldBe false
        }
    }
}
