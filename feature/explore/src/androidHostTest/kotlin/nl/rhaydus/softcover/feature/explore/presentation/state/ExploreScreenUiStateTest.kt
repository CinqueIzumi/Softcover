package nl.rhaydus.softcover.feature.explore.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import nl.rhaydus.softcover.feature.explore.domain.model.MoodTag

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

        @Test
        fun `isLoading is LOADING even when search is focused`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                isLoading = true,
                searchFocused = true,
            )

            // ----- Act -----
            val result = state.searchPhase

            // ----- Assert -----
            result shouldBe ExploreSearchPhase.LOADING
        }

        @Test
        fun `isLoading with no other flags set is still LOADING`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(isLoading = true)

            // ----- Act -----
            val result = state.searchPhase

            // ----- Assert -----
            result shouldBe ExploreSearchPhase.LOADING
        }

        @Test
        fun `RESULTS wins over FOCUS when both searchText and searchFocused are set`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState(
                searchText = "kotlin",
                searchFocused = true,
                isLoading = false,
            )

            // ----- Act -----
            val result = state.searchPhase

            // ----- Assert -----
            result shouldBe ExploreSearchPhase.RESULTS
        }
    }
}
