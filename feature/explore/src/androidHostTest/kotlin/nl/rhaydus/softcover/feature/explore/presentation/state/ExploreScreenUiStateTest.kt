package nl.rhaydus.softcover.feature.explore.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.state.SkeletonVariant
import nl.rhaydus.softcover.feature.explore.domain.model.FEATURED_RELEASE_WINDOW_DAYS
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

    @Nested
    inner class HasActiveSearch {
        @Test
        fun `unfocused empty query with no mood filter is false`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState()

            // ----- Act -----
            val result = state.hasActiveSearch

            // ----- Assert -----
            result shouldBe false
        }

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
    inner class Defaults {
        @Test
        fun `featuredHeader states the release window from the domain constant`() {
            // ----- Arrange -----
            val state = ExploreScreenUiState()

            // ----- Act -----
            val result = state.featuredHeader

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Inline(
                eyebrow = "Most anticipated · next $FEATURED_RELEASE_WINDOW_DAYS days",
            )
        }

        @Test
        fun `trendingSkeleton defaults to the ExploreRail variant`() {
            // ----- Arrange & Act -----
            val state = ExploreScreenUiState()

            // ----- Assert -----
            state.trendingSkeleton.variant shouldBe SkeletonVariant.ExploreRail
        }

        @Test
        fun `seriesSkeleton defaults to the ExploreSeries variant`() {
            // ----- Arrange & Act -----
            val state = ExploreScreenUiState()

            // ----- Assert -----
            state.seriesSkeleton.variant shouldBe SkeletonVariant.ExploreSeries
        }

        @Test
        fun `featuredSkeleton defaults to the ExploreFeatured variant`() {
            // ----- Arrange & Act -----
            val state = ExploreScreenUiState()

            // ----- Assert -----
            state.featuredSkeleton.variant shouldBe SkeletonVariant.ExploreFeatured
        }

        @Test
        fun `becauseYouReadHeaderSkeleton defaults to the ExploreSectionHeader variant`() {
            // ----- Arrange & Act -----
            val state = ExploreScreenUiState()

            // ----- Assert -----
            state.becauseYouReadHeaderSkeleton.variant shouldBe SkeletonVariant.ExploreSectionHeader
        }

        @Test
        fun `becauseYouReadSkeleton defaults to the ExploreRail variant`() {
            // ----- Arrange & Act -----
            val state = ExploreScreenUiState()

            // ----- Assert -----
            state.becauseYouReadSkeleton.variant shouldBe SkeletonVariant.ExploreRail
        }

        @Test
        fun `moodTileSkeleton defaults to the ExploreMoodTile variant`() {
            // ----- Arrange & Act -----
            val state = ExploreScreenUiState()

            // ----- Assert -----
            state.moodTileSkeleton.variant shouldBe SkeletonVariant.ExploreMoodTile
        }
    }
}
