package nl.rhaydus.softcover.core.profile.domain.model

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuthorDemographicsTest {
    private fun demographics(
        genderSlices: List<GenderSlice> = emptyList(),
        knownGenderCount: Int = 0,
        unknownGenderCount: Int = 0,
        bipocBreakdown: DemographicBreakdown = DemographicBreakdown(),
        lgbtqBreakdown: DemographicBreakdown = DemographicBreakdown(),
    ): AuthorDemographics = AuthorDemographics(
        genderSlices = genderSlices,
        knownGenderCount = knownGenderCount,
        unknownGenderCount = unknownGenderCount,
        bipocBreakdown = bipocBreakdown,
        lgbtqBreakdown = lgbtqBreakdown,
    )

    @Nested
    inner class ExcludingBooksWithoutDetail {
        @Test
        fun `each remaining gender slice's fraction equals its count over known gender count`() {
            // ----- Arrange -----
            val original = demographics(
                genderSlices = listOf(
                    GenderSlice(
                        gender = AuthorshipGender.Male,
                        count = 3,
                        fraction = 0.3,
                    ),
                    GenderSlice(
                        gender = AuthorshipGender.Female,
                        count = 6,
                        fraction = 0.6,
                    ),
                    GenderSlice(
                        gender = AuthorshipGender.Unknown,
                        count = 1,
                        fraction = 0.1,
                    ),
                ),
                knownGenderCount = 9,
                unknownGenderCount = 1,
            )

            // ----- Act -----
            val result = original.excludingBooksWithoutDetail()

            // ----- Assert -----
            result.genderSlices.forEach { slice ->
                slice.fraction shouldBe (slice.count.toDouble() / 9 plusOrMinus 0.0001)
            }
        }

        @Test
        fun `drops the Unknown gender slice and zeroes unknownGenderCount`() {
            // ----- Arrange -----
            val original = demographics(
                genderSlices = listOf(
                    GenderSlice(
                        gender = AuthorshipGender.Male,
                        count = 3,
                        fraction = 0.3,
                    ),
                    GenderSlice(
                        gender = AuthorshipGender.Unknown,
                        count = 1,
                        fraction = 0.1,
                    ),
                ),
                knownGenderCount = 3,
                unknownGenderCount = 1,
            )

            // ----- Act -----
            val result = original.excludingBooksWithoutDetail()

            // ----- Assert -----
            result.genderSlices.none { it.gender == AuthorshipGender.Unknown } shouldBe true
            result.unknownGenderCount shouldBe 0
        }

        @Test
        fun `zeroes unknownCount on both breakdowns while keeping yes and no, shrinking total`() {
            // ----- Arrange -----
            val original = demographics(
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

            // ----- Act -----
            val result = original.excludingBooksWithoutDetail()

            // ----- Assert -----
            result.bipocBreakdown shouldBe DemographicBreakdown(
                yesCount = 4,
                noCount = 5,
                unknownCount = 0,
            )

            result.bipocBreakdown.total shouldBe 9

            result.lgbtqBreakdown shouldBe DemographicBreakdown(
                yesCount = 1,
                noCount = 2,
                unknownCount = 0,
            )

            result.lgbtqBreakdown.total shouldBe 3
        }

        @Test
        fun `Mixed survives the exclusion and is renormalised with the known genders`() {
            // ----- Arrange -----
            val original = demographics(
                genderSlices = listOf(
                    GenderSlice(
                        gender = AuthorshipGender.Female,
                        count = 2,
                        fraction = 0.4,
                    ),
                    GenderSlice(
                        gender = AuthorshipGender.Mixed,
                        count = 2,
                        fraction = 0.4,
                    ),
                    GenderSlice(
                        gender = AuthorshipGender.Unknown,
                        count = 1,
                        fraction = 0.2,
                    ),
                ),
                knownGenderCount = 4,
                unknownGenderCount = 1,
            )

            // ----- Act -----
            val result = original.excludingBooksWithoutDetail()

            // ----- Assert -----
            result.genderSlices.map { it.gender } shouldBe listOf(AuthorshipGender.Female, AuthorshipGender.Mixed)
            result.genderSlices.map { it.fraction } shouldBe listOf(0.5, 0.5)
        }

        @Test
        fun `known gender count of zero returns empty gender slices rather than dividing by zero`() {
            // ----- Arrange -----
            val original = demographics(
                genderSlices = listOf(
                    GenderSlice(
                        gender = AuthorshipGender.Unknown,
                        count = 5,
                        fraction = 1.0,
                    ),
                ),
                knownGenderCount = 0,
                unknownGenderCount = 5,
            )

            // ----- Act -----
            val result = original.excludingBooksWithoutDetail()

            // ----- Assert -----
            result.genderSlices shouldBe emptyList()
        }

        @Test
        fun `a breakdown that is entirely unknown collapses to total zero`() {
            // ----- Arrange -----
            val original = demographics(
                bipocBreakdown = DemographicBreakdown(unknownCount = 7),
                lgbtqBreakdown = DemographicBreakdown(unknownCount = 4),
            )

            // ----- Act -----
            val result = original.excludingBooksWithoutDetail()

            // ----- Assert -----
            result.bipocBreakdown.total shouldBe 0
            result.lgbtqBreakdown.total shouldBe 0
        }

        @Test
        fun `an input with nothing unknown at all comes back unchanged`() {
            // ----- Arrange -----
            val original = demographics(
                genderSlices = listOf(
                    GenderSlice(
                        gender = AuthorshipGender.Male,
                        count = 4,
                        fraction = 0.5,
                    ),
                    GenderSlice(
                        gender = AuthorshipGender.Female,
                        count = 4,
                        fraction = 0.5,
                    ),
                ),
                knownGenderCount = 8,
                unknownGenderCount = 0,
                bipocBreakdown = DemographicBreakdown(
                    yesCount = 3,
                    noCount = 5,
                    unknownCount = 0,
                ),
                lgbtqBreakdown = DemographicBreakdown(
                    yesCount = 2,
                    noCount = 6,
                    unknownCount = 0,
                ),
            )

            // ----- Act -----
            val result = original.excludingBooksWithoutDetail()

            // ----- Assert -----
            result shouldBe original
        }
    }
}
