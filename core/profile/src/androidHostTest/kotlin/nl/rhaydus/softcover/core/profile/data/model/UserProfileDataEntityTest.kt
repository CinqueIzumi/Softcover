package nl.rhaydus.softcover.core.profile.data.model

import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import nl.rhaydus.softcover.core.profile.domain.model.GenreSlice
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class UserProfileDataEntityTest {
    private fun minimalModel(dates: Set<LocalDate> = emptySet()): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 5,
        recentReadingDays = dates,
    )

    // Populates every reading-life field added by the Profile "reading life" redesign, with the
    // ratings band deliberately set to a non-default value to prove it is intentionally dropped
    // by the entity (see RatingsDistributionEntity - band is never persisted).
    private fun richModel(): UserProfileData = UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = "Jane Doe",
        username = "cinque",
        bio = "Avid reader",
        booksRead = 42,
        totalPagesRead = 12000,
        averageRating = 4.2,
        readingStreak = 5,
        booksByYear = listOf(YearCount(
            year = 2025,
            count = 10,
        ), YearCount(
            year = 2026,
            count = 5,
        ),),
        pagesByYear = listOf(YearCount(
            year = 2025,
            count = 3000,
        ), YearCount(
            year = 2026,
            count = 1500,
        ),),
        pagesByMonth = listOf(MonthCount(
            year = 2026,
            month = 1,
            count = 300,
        ), MonthCount(
            year = 2026,
            month = 2,
            count = 450,
        ),),
        genres = listOf(GenreSlice(
            name = "Fantasy",
            count = 10,
            fraction = 0.5,
        ), GenreSlice(
            name = "Sci-Fi",
            count = 10,
            fraction = 0.5,
        ),),
        ratings = RatingsDistribution(
            average = 3.75,
            totalRatings = 8,
            halfStarBuckets = listOf(0, 0, 1, 0, 2, 0, 3, 1, 1),
            band = RatingBand.SUPERFAN,
        ),
        recentlyLoved = listOf(
            LovedBook(
                coverUrl = "cover.jpg",
                title = "Dune",
                author = "Frank Herbert",
                ratingStars = 4.5,
                monthLabel = "May 2026",
            ),
        ),
    )

    @Nested
    inner class RoundTrip {
        @Test
        fun `toEntity then toModel preserves an empty active dates set`() {
            // ----- Arrange -----
            val model = minimalModel(dates = emptySet())

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.recentReadingDays shouldBe emptySet()
        }

        @Test
        fun `toEntity then toModel preserves a non-empty active dates set`() {
            // ----- Arrange -----
            val dates = setOf(
                LocalDate(
                    2026,
                    4,
                    14,
                ),
                LocalDate(
                    2026,
                    4,
                    20,
                ),
                LocalDate(
                    2026,
                    5,
                    4,
                ),
            )
            val model = minimalModel(dates = dates)

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.recentReadingDays shouldBe dates
        }

        @Test
        fun `toEntity then toModel preserves username`() {
            // ----- Arrange -----
            val model = minimalModel()

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.username shouldBe "cinque"
        }

        @Test
        fun `toEntity then toModel preserves trackedYears`() {
            // ----- Arrange -----
            val model = minimalModel().copy(trackedYears = 6)

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.trackedYears shouldBe 6
        }

        @Test
        fun `round-trips booksByYear, pagesByYear, pagesByMonth, genres and recentlyLoved`() {
            // ----- Arrange -----
            val model = richModel()

            // ----- Act -----
            val entity = model.toEntity()
            val result = entity.toModel()

            // ----- Assert -----
            result.booksByYear shouldBe model.booksByYear
            result.pagesByYear shouldBe model.pagesByYear
            result.pagesByMonth shouldBe model.pagesByMonth
            result.genres shouldBe model.genres
            result.recentlyLoved shouldBe model.recentlyLoved
        }

        @Test
        fun `round-trips ratings average, totalRatings and halfStarBuckets, but never the band`() {
            // ----- Arrange -----
            val model = richModel()

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.ratings.average shouldBe model.ratings.average
            result.ratings.totalRatings shouldBe model.ratings.totalRatings
            result.ratings.halfStarBuckets shouldBe model.ratings.halfStarBuckets
            result.ratings.band shouldBe RatingBand.NEW_READER
        }

        @Test
        fun `toModel substitutes the canonical 9-bucket zero vector when the cached halfStarBuckets is empty`() {
            // ----- Arrange -----
            val entity = minimalModel().toEntity().copy(ratings = RatingsDistributionEntity())

            // ----- Act -----
            val result = entity.toModel()

            // ----- Assert -----
            result.ratings.halfStarBuckets shouldBe List(9) { 0 }
        }

        @Test
        fun `toEntity serialises dates as ascending ISO strings`() {
            // ----- Arrange -----
            val dates = setOf(
                LocalDate(
                    2026,
                    5,
                    4,
                ),
                LocalDate(
                    2026,
                    4,
                    14,
                ),
                LocalDate(
                    2026,
                    4,
                    30,
                ),
            )
            val model = minimalModel(dates = dates)

            // ----- Act -----
            val entity = model.toEntity()

            // ----- Assert -----
            entity.recentReadingDays shouldBe listOf("2026-04-14", "2026-04-30", "2026-05-04")
        }
    }

    @Nested
    inner class AuthorDemographicsRoundTrip {
        // A gender breakdown that includes both Other and an Unknown slice, plus a mixed
        // bipocBreakdown and an all-unknown lgbtqBreakdown, so the round trip exercises every
        // branch of the entity mapping (see GenderSliceEntity - gender is persisted as its raw
        // genderId).
        private fun authorDemographics(): AuthorDemographics = AuthorDemographics(
            genderSlices = listOf(
                GenderSlice(
                    gender = Gender.Male,
                    count = 3,
                    fraction = 3.0 / 7.0,
                ),
                GenderSlice(
                    gender = Gender.Other,
                    count = 2,
                    fraction = 2.0 / 7.0,
                ),
                GenderSlice(
                    gender = Gender.Unknown,
                    count = 2,
                    fraction = 2.0 / 7.0,
                ),
            ),
            knownGenderCount = 5,
            unknownGenderCount = 2,
            bipocBreakdown = DemographicBreakdown(
                yesCount = 2,
                noCount = 3,
                unknownCount = 0,
            ),
            lgbtqBreakdown = DemographicBreakdown(
                yesCount = 0,
                noCount = 0,
                unknownCount = 5,
            ),
        )

        @Test
        fun `toEntity then toModel preserves gender slices - including Other and Unknown - counts and breakdowns`() {
            // ----- Arrange -----
            val model = minimalModel().copy(authorDemographics = authorDemographics())

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.authorDemographics shouldBe authorDemographics()
        }

        @Test
        fun `genderId persistence rebuilds Gender_Other from id 3 and Gender_Unknown from a null id`() {
            // ----- Arrange -----
            val model = minimalModel().copy(authorDemographics = authorDemographics())

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.authorDemographics.genderSlices.map { it.gender } shouldBe listOf(
                Gender.Male,
                Gender.Other,
                Gender.Unknown,
            )
        }

        @Test
        fun `an all-unknown bipocBreakdown and lgbtqBreakdown both round-trip as DemographicBreakdown(0, 0, N)`() {
            // ----- Arrange -----
            val model = minimalModel().copy(
                authorDemographics = AuthorDemographics(
                    bipocBreakdown = DemographicBreakdown(unknownCount = 4),
                    lgbtqBreakdown = DemographicBreakdown(unknownCount = 4),
                ),
            )

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.authorDemographics.bipocBreakdown shouldBe DemographicBreakdown(unknownCount = 4)
            result.authorDemographics.lgbtqBreakdown shouldBe DemographicBreakdown(unknownCount = 4)
        }

        @Test
        fun `authorDemographics defaults to an empty AuthorDemographics when never set`() {
            // ----- Arrange -----
            val model = minimalModel()

            // ----- Act -----
            val result = model.toEntity().toModel()

            // ----- Assert -----
            result.authorDemographics shouldBe AuthorDemographics()
        }
    }

    @Nested
    inner class BackwardCompat {
        @Test
        fun `legacy JSON without recentReadingDays field decodes with empty list`() {
            // ----- Arrange -----
            val legacyJson = """
                {
                  "profileImageUrl": "https://example.com/avatar.png",
                  "name": "Jane Doe",
                  "bio": "Avid reader",
                  "booksRead": 42,
                  "totalPagesRead": 12000,
                  "averageRating": 4.2,
                  "readingStreak": 5
                }
            """.trimIndent()
            val lenientJson = Json { ignoreUnknownKeys = true }

            // ----- Act -----
            val entity = lenientJson.decodeFromString(
                UserProfileDataEntity.serializer(),
                legacyJson,
            )

            // ----- Assert -----
            entity.recentReadingDays shouldBe emptyList()
        }

        @Test
        fun `legacy entity without recentReadingDays maps to empty set on toModel`() {
            // ----- Arrange -----
            val legacyJson = """
                {
                  "profileImageUrl": "https://example.com/avatar.png",
                  "name": "Jane Doe",
                  "bio": "Avid reader",
                  "booksRead": 42,
                  "totalPagesRead": 12000,
                  "averageRating": 4.2,
                  "readingStreak": 5
                }
            """.trimIndent()
            val lenientJson = Json { ignoreUnknownKeys = true }

            // ----- Act -----
            val entity = lenientJson.decodeFromString(
                UserProfileDataEntity.serializer(),
                legacyJson,
            )
            val model = entity.toModel()

            // ----- Assert -----
            model.recentReadingDays shouldBe emptySet()
        }

        @Test
        fun `legacy entity without trackedYears maps to default 0 on toModel`() {
            // ----- Arrange -----
            val legacyJson = """
                {
                  "profileImageUrl": "https://example.com/avatar.png",
                  "name": "Jane Doe",
                  "bio": "Avid reader",
                  "booksRead": 42,
                  "totalPagesRead": 12000,
                  "averageRating": 4.2,
                  "readingStreak": 5
                }
            """.trimIndent()
            val lenientJson = Json { ignoreUnknownKeys = true }

            // ----- Act -----
            val entity = lenientJson.decodeFromString(
                UserProfileDataEntity.serializer(),
                legacyJson,
            )
            val model = entity.toModel()

            // ----- Assert -----
            model.trackedYears shouldBe 0
        }

        @Test
        fun `legacy JSON without an authorDemographics key decodes to the empty AuthorDemographics default`() {
            // ----- Arrange -----
            val legacyJson = """
                {
                  "profileImageUrl": "https://example.com/avatar.png",
                  "name": "Jane Doe",
                  "bio": "Avid reader",
                  "booksRead": 42,
                  "totalPagesRead": 12000,
                  "averageRating": 4.2,
                  "readingStreak": 5
                }
            """.trimIndent()
            val lenientJson = Json { ignoreUnknownKeys = true }

            // ----- Act -----
            val entity = lenientJson.decodeFromString(
                UserProfileDataEntity.serializer(),
                legacyJson,
            )
            val model = entity.toModel()

            // ----- Assert -----
            model.authorDemographics shouldBe AuthorDemographics()
        }

        @Test
        fun `legacy JSON carrying the old bipocShare-lgbtqShare keys ignores them and decodes to empty breakdowns`() {
            // ----- Arrange -----
            // Pre-migration cache blobs nested a fractional "bipocShare"/"lgbtqShare" object under
            // authorDemographics instead of the current "bipocBreakdown"/"lgbtqBreakdown". Those old
            // keys must be ignored rather than crash decoding, and since the new breakdown keys are
            // themselves absent, they fall back to the empty DemographicBreakdown default.
            val legacyJson = """
                {
                  "profileImageUrl": "https://example.com/avatar.png",
                  "name": "Jane Doe",
                  "bio": "Avid reader",
                  "booksRead": 42,
                  "totalPagesRead": 12000,
                  "averageRating": 4.2,
                  "readingStreak": 5,
                  "authorDemographics": {
                    "genderSlices": [],
                    "knownGenderCount": 0,
                    "unknownGenderCount": 0,
                    "bipocShare": {
                      "trueCount": 2,
                      "knownCount": 5,
                      "fraction": 0.4
                    },
                    "lgbtqShare": null
                  }
                }
            """.trimIndent()
            val lenientJson = Json { ignoreUnknownKeys = true }

            // ----- Act -----
            val entity = lenientJson.decodeFromString(
                UserProfileDataEntity.serializer(),
                legacyJson,
            )
            val model = entity.toModel()

            // ----- Assert -----
            model.authorDemographics.bipocBreakdown shouldBe DemographicBreakdown()
            model.authorDemographics.lgbtqBreakdown shouldBe DemographicBreakdown()
        }

        @Test
        fun `malformed date strings in recentReadingDays are silently dropped on toModel`() {
            // ----- Arrange -----
            val entity = UserProfileDataEntity(
                profileImageUrl = "https://example.com/avatar.png",
                name = "Jane Doe",
                username = "cinque",
                bio = "Avid reader",
                booksRead = 42,
                totalPagesRead = 12000,
                averageRating = 4.2,
                readingStreak = 5,
                recentReadingDays = listOf("2026-05-04", "not-a-date", "2026-04-14"),
            )

            // ----- Act -----
            val model = entity.toModel()

            // ----- Assert -----
            model.recentReadingDays shouldBe setOf(
                LocalDate(
                    2026,
                    5,
                    4,
                ),
                LocalDate(
                    2026,
                    4,
                    14,
                ),
            )
        }
    }
}
