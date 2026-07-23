package nl.rhaydus.softcover.core.profile.domain.usecase

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ObserveReadingLifeUseCaseTest {
    private lateinit var observeUserProfileDataUseCase: ObserveUserProfileDataUseCase
    private lateinit var useCase: ObserveReadingLifeUseCase

    @BeforeEach
    fun setUp() {
        observeUserProfileDataUseCase = mockk()
        useCase = ObserveReadingLifeUseCase(observeUserProfileDataUseCase = observeUserProfileDataUseCase)
    }

    private fun profileData(
        ratings: RatingsDistribution,
        authorDemographics: AuthorDemographics = AuthorDemographics(),
    ): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 5,
        booksByYear = listOf(YearCount(
            year = 2026,
            count = 5,
        ),),
        pagesByYear = listOf(YearCount(
            year = 2026,
            count = 1500,
        ),),
        pagesByMonth = listOf(MonthCount(
            year = 2026,
            month = 3,
            count = 300,
        ),),
        genres = GenreBreakdown(
            slices = listOf(GenreSlice(
                name = "Fantasy",
                count = 5,
                fraction = 1.0,
            ),),
            taggedBookCount = 5,
        ),
        ratings = ratings,
        recentlyLoved = listOf(
            LovedBook(
                coverUrl = "cover.jpg",
                title = "Dune",
                author = "Frank Herbert",
                ratingStars = 4.5,
                monthLabel = "May 2026",
            ),
        ),
        trackedYears = 4,
        authorDemographics = authorDemographics,
    )

    @Nested
    inner class Invoke {
        @Test
        fun `maps a cached profile to ReadingLife, carrying the list fields straight through`() = runTest {
            // ----- Arrange -----
            val ratings = RatingsDistribution(
                average = 4.0,
                totalRatings = 20,
                halfStarBuckets = List(9) { 0 },
            )
            val data = profileData(ratings = ratings)

            every {
                observeUserProfileDataUseCase()
            } returns flowOf(data)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.booksByYear shouldBe data.booksByYear
            result?.pagesByYear shouldBe data.pagesByYear
            result?.pagesByMonth shouldBe data.pagesByMonth
            result?.genres shouldBe data.genres
            result?.recentlyLoved shouldBe data.recentlyLoved
            result?.trackedYears shouldBe data.trackedYears
        }

        @Test
        fun `recomputes band fresh instead of trusting a stale cached value`() = runTest {
            // ----- Arrange -----
            // The cached band claims SUPERFAN, but the average/buckets/total actually describe an
            // EXACTING reader - the use case must not trust the stale cached band.
            val staleRatings = RatingsDistribution(
                average = 2.0,
                totalRatings = 20,
                halfStarBuckets = listOf(10, 5, 5, 0, 0, 0, 0, 0, 0),
                band = RatingBand.SUPERFAN,
            )

            every {
                observeUserProfileDataUseCase()
            } returns flowOf(profileData(ratings = staleRatings))

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.ratings?.band shouldBe RatingBand.EXACTING
        }

        @Test
        fun `authorDemographics carries straight through to ReadingLife`() = runTest {
            // ----- Arrange -----
            val demographics = AuthorDemographics(
                genderSlices = listOf(GenderSlice(
                    gender = Gender.Other,
                    count = 2,
                    fraction = 1.0,
                ),),
                knownGenderCount = 2,
                lgbtqBreakdown = DemographicBreakdown(
                    yesCount = 1,
                    noCount = 1,
                    unknownCount = 0,
                ),
            )
            val ratings = RatingsDistribution(
                average = 4.0,
                totalRatings = 20,
                halfStarBuckets = List(9) { 0 },
            )

            every {
                observeUserProfileDataUseCase()
            } returns flowOf(profileData(
                ratings = ratings,
                authorDemographics = demographics,
            ),)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result?.authorDemographics shouldBe demographics
        }

        @Test
        fun `emits null when the cached profile is null`() = runTest {
            // ----- Arrange -----
            every {
                observeUserProfileDataUseCase()
            } returns flowOf(null)

            // ----- Act -----
            val result = useCase().first()

            // ----- Assert -----
            result shouldBe null
        }
    }
}
