package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
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
    inner class AuthorDemographicsProperty {
        @Test
        fun `returns the default empty AuthorDemographics when readingLife is null`() {
            // ----- Arrange -----
            val state = ProfileUiState(
                readingLife = null,
                hideUntaggedAuthors = false,
            )

            // ----- Act -----
            val result = state.authorDemographics

            // ----- Assert -----
            result shouldBe AuthorDemographics()
        }

        @Test
        fun `returns the reading life's demographics unchanged when the flag is off`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                genderSlices = listOf(
                    GenderSlice(
                        gender = Gender.Male,
                        count = 3,
                        fraction = 0.3,
                    ),
                    GenderSlice(
                        gender = Gender.Unknown,
                        count = 7,
                        fraction = 0.7,
                    ),
                ),
                knownGenderCount = 3,
                unknownGenderCount = 7,
                bipocBreakdown = DemographicBreakdown(
                    yesCount = 1,
                    noCount = 1,
                    unknownCount = 1,
                ),
                lgbtqBreakdown = DemographicBreakdown(
                    yesCount = 1,
                    noCount = 1,
                    unknownCount = 1,
                ),
            )
            val state = ProfileUiState(
                readingLife = readingLifeWith(demographics),
                hideUntaggedAuthors = false,
            )

            // ----- Act -----
            val result = state.authorDemographics

            // ----- Assert -----
            result shouldBe demographics
        }

        @Test
        fun `returns the excludingUntaggedAuthors reading when the flag is on`() {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                genderSlices = listOf(
                    GenderSlice(
                        gender = Gender.Male,
                        count = 3,
                        fraction = 0.3,
                    ),
                    GenderSlice(
                        gender = Gender.Female,
                        count = 6,
                        fraction = 0.6,
                    ),
                    GenderSlice(
                        gender = Gender.Unknown,
                        count = 1,
                        fraction = 0.1,
                    ),
                ),
                knownGenderCount = 9,
                unknownGenderCount = 1,
                bipocBreakdown = DemographicBreakdown(
                    yesCount = 4,
                    noCount = 5,
                    unknownCount = 6,
                ),
                lgbtqBreakdown = DemographicBreakdown(
                    yesCount = 1,
                    noCount = 2,
                    unknownCount = 3,
                ),
            )
            val state = ProfileUiState(
                readingLife = readingLifeWith(demographics),
                hideUntaggedAuthors = true,
            )

            // ----- Act -----
            val result = state.authorDemographics

            // ----- Assert -----
            val maleSlice = result.genderSlices.first { it.gender == Gender.Male }
            maleSlice.fraction shouldBe (3.0 / 9 plusOrMinus 0.0001)
            result.bipocBreakdown.unknownCount shouldBe 0
        }
    }

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
}
