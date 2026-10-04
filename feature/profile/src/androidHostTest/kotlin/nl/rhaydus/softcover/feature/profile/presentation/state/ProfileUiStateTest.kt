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

class ProfileUiStateTest {
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
            val state = ProfileUiState(
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
            val state = ProfileUiState(
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
            val state = ProfileUiState(
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
            val state = ProfileUiState(readingLife = null)

            // ----- Act -----
            val result = state.hasRecentlyLoved

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when readingLife has no loved books`() {
            // ----- Arrange -----
            val state = ProfileUiState(readingLife = readingLifeLoving(emptyList()))

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
            val state = ProfileUiState(readingLife = readingLifeLoving(listOf(loved)))

            // ----- Act -----
            val result = state.hasRecentlyLoved

            // ----- Assert -----
            result shouldBe true
        }
    }
}
