package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.RecentlyLovedBook
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_KEY
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.statsScopeKey
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StatsAtlasUiStateTest {
    private fun statsWith(authorDemographics: AuthorDemographics): ReadingStats =
        ReadingStats(authorDemographics = authorDemographics)

    @Nested
    inner class HasUntaggedAuthorsProperty {
        @Test
        fun `is true when only the bipoc breakdown has unknowns`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                unknownGenderCount = 0,
                bipocBreakdown = DemographicBreakdown(
                    yesCount = 4,
                    noCount = 5,
                    unknownCount = 1,
                ),
                lgbtqBreakdown = DemographicBreakdown(
                    yesCount = 1,
                    noCount = 2,
                    unknownCount = 0,
                ),
            )
            val state = StatsAtlasUiState(
                readingStats = statsWith(demographics),
                hideUntaggedAuthors = false,
            )

            // ----- Act -----
            val result = state.hasUntaggedAuthors

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false when nothing is unknown across gender, bipoc and lgbtq`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                unknownGenderCount = 0,
                bipocBreakdown = DemographicBreakdown(
                    yesCount = 4,
                    noCount = 5,
                    unknownCount = 0,
                ),
                lgbtqBreakdown = DemographicBreakdown(
                    yesCount = 1,
                    noCount = 2,
                    unknownCount = 0,
                ),
            )
            val state = StatsAtlasUiState(
                readingStats = statsWith(demographics),
                hideUntaggedAuthors = false,
            )

            // ----- Act -----
            val result = state.hasUntaggedAuthors

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `stays true while hideUntaggedAuthors is on because it reads the unfiltered data`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                unknownGenderCount = 2,
                bipocBreakdown = DemographicBreakdown(
                    yesCount = 4,
                    noCount = 5,
                    unknownCount = 1,
                ),
                lgbtqBreakdown = DemographicBreakdown(
                    yesCount = 1,
                    noCount = 2,
                    unknownCount = 0,
                ),
            )
            val state = StatsAtlasUiState(
                readingStats = statsWith(demographics),
                hideUntaggedAuthors = true,
            )

            // ----- Act -----
            val result = state.hasUntaggedAuthors

            // ----- Assert -----
            result shouldBe true
        }
    }

    @Nested
    inner class HasRecentlyLovedProperty {
        private fun historyLoving(recentlyLoved: List<RecentlyLovedBook>): ReadingHistory =
            ReadingHistory(recentlyLoved = recentlyLoved)

        @Test
        fun `is false when readingHistory is null`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(readingHistory = null)

            // ----- Act -----
            val result = state.hasRecentlyLoved

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when readingHistory has no loved books`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(readingHistory = historyLoving(emptyList()))

            // ----- Act -----
            val result = state.hasRecentlyLoved

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is true when readingHistory has a loved book`() {
            // ----- Arrange -----
            val loved = RecentlyLovedBook(
                bookId = 1,
                ratingStars = 5.0,
                finishedOn = LocalDate(
                    year = 2026,
                    month = 5,
                    day = 1,
                ),
            )
            val state = StatsAtlasUiState(readingHistory = historyLoving(listOf(loved)))

            // ----- Act -----
            val result = state.hasRecentlyLoved

            // ----- Assert -----
            result shouldBe true
        }
    }

    @Nested
    inner class IsReadingLifeLoadingProperty {
        @Test
        fun `is true while the profile is still loading even with stats and history`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(
                isLoading = true,
                readingStats = ReadingStats(),
                readingHistory = ReadingHistory(),
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is true when the reading stats are missing`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(
                isLoading = false,
                readingStats = null,
                readingHistory = ReadingHistory(),
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is true when the reading history is missing`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(
                isLoading = false,
                readingStats = ReadingStats(),
                readingHistory = null,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false once all of them have loaded`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(
                isLoading = false,
                readingStats = ReadingStats(),
                readingHistory = ReadingHistory(),
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe false
        }
    }

    @Nested
    inner class RatingsTotalProperty {
        @Test
        fun `is zero while the reading stats are missing`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(readingStats = null)

            // ----- Act -----
            val result = state.ratingsTotal

            // ----- Assert -----
            result shouldBe 0
        }

        @Test
        fun `reads the total from the reading stats ratings`() {
            // ----- Arrange -----
            val stats = ReadingStats(ratings = RatingsDistribution(totalRatings = 17))
            val state = StatsAtlasUiState(readingStats = stats)

            // ----- Act -----
            val result = state.ratingsTotal

            // ----- Assert -----
            result shouldBe 17
        }
    }

    @Nested
    inner class SelectedScopeProperty {
        @Test
        fun `resolves the selected key through the chip set`() {
            // ----- Arrange -----
            val key = statsScopeKey(StatsScope.CalendarYear(2025))
            val state = StatsAtlasUiState(
                selectedScopeKey = key,
                scopeChips = atlasScopeChips(
                    yearsRead = listOf(2026, 2025),
                    selectedKey = key,
                ),
            )

            // ----- Act -----
            val result = state.selectedScope

            // ----- Assert -----
            result shouldBe StatsScope.CalendarYear(2025)
        }

        @Test
        fun `falls back to all time while the chip set does not hold the key`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(
                selectedScopeKey = statsScopeKey(StatsScope.CalendarYear(2025)),
                scopeChips = atlasScopeChips(
                    yearsRead = emptyList(),
                    selectedKey = ALL_TIME_SCOPE_KEY,
                ),
            )

            // ----- Act -----
            val result = state.selectedScope

            // ----- Assert -----
            result shouldBe StatsScope.AllTime
        }
    }
}
