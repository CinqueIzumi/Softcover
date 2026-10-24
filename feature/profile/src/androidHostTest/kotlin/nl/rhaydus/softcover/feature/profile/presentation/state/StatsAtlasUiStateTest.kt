package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StatsAtlasUiStateTest {
    private fun readingLifeWith(authorDemographics: AuthorDemographics): ReadingLife = ReadingLife(
        booksByYear = emptyList(),
        pagesByYear = emptyList(),
        pagesByMonth = emptyList(),
        genres = GenreBreakdown(),
        ratings = RatingsDistribution(),
        recentlyLoved = emptyList(),
        authorDemographics = authorDemographics,
    )

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
                readingLife = readingLifeWith(demographics),
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
                readingLife = readingLifeWith(demographics),
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
                readingLife = readingLifeWith(demographics),
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
        private fun readingLifeLoving(recentlyLoved: List<LovedBook>): ReadingLife =
            readingLifeWith(AuthorDemographics()).copy(recentlyLoved = recentlyLoved)

        @Test
        fun `is false when readingLife is null`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(readingLife = null)

            // ----- Act -----
            val result = state.hasRecentlyLoved

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when readingLife has no loved books`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(readingLife = readingLifeLoving(emptyList()))

            // ----- Act -----
            val result = state.hasRecentlyLoved

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is true when readingLife has a loved book`() {
            // ----- Arrange -----
            val loved = LovedBook(
                bookId = 1,
                coverUrl = "",
                title = "Title",
                author = "Author",
                ratingStars = 5.0,
                monthLabel = "May",
            )
            val state = StatsAtlasUiState(readingLife = readingLifeLoving(listOf(loved)))

            // ----- Act -----
            val result = state.hasRecentlyLoved

            // ----- Assert -----
            result shouldBe true
        }
    }

    @Nested
    inner class IsReadingLifeLoadingProperty {
        @Test
        fun `is true while the profile is still loading even with a reading life`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(
                isLoading = true,
                readingLife = readingLifeWith(AuthorDemographics()),
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is true when the reading life is missing`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(
                isLoading = false,
                readingLife = null,
            )

            // ----- Act -----
            val result = state.isReadingLifeLoading

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false once both have loaded`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(
                isLoading = false,
                readingLife = readingLifeWith(AuthorDemographics()),
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
        fun `is zero while the reading life is missing`() {
            // ----- Arrange -----
            val state = StatsAtlasUiState(readingLife = null)

            // ----- Act -----
            val result = state.ratingsTotal

            // ----- Assert -----
            result shouldBe 0
        }

        @Test
        fun `reads the total from the reading life ratings`() {
            // ----- Arrange -----
            val readingLife = readingLifeWith(AuthorDemographics())
                .copy(ratings = RatingsDistribution(totalRatings = 17))
            val state = StatsAtlasUiState(readingLife = readingLife)

            // ----- Act -----
            val result = state.ratingsTotal

            // ----- Assert -----
            result shouldBe 17
        }
    }
}
