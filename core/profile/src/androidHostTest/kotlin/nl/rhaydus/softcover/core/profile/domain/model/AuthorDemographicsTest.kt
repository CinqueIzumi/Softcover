package nl.rhaydus.softcover.core.profile.domain.model

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Gender
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
    inner class ExcludingUntaggedAuthors {
        @Test
        fun `renormalises remaining gender slices so fractions sum to 1`() {
            // ----- Arrange -----
            val original = demographics(
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
            )

            // ----- Act -----
            val result = original.excludingUntaggedAuthors()

            // ----- Assert -----
            val fractionSum = result.genderSlices.sumOf { it.fraction }
            fractionSum shouldBe (1.0 plusOrMinus 0.0001)
        }

        @Test
        fun `each remaining gender slice's fraction equals its count over known gender count`() {
            // ----- Arrange -----
            val original = demographics(
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
            )

            // ----- Act -----
            val result = original.excludingUntaggedAuthors()

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
                        gender = Gender.Male,
                        count = 3,
                        fraction = 0.3,
                    ),
                    GenderSlice(
                        gender = Gender.Unknown,
                        count = 1,
                        fraction = 0.1,
                    ),
                ),
                knownGenderCount = 3,
                unknownGenderCount = 1,
            )

            // ----- Act -----
            val result = original.excludingUntaggedAuthors()

            // ----- Assert -----
            result.genderSlices.none { it.gender == Gender.Unknown } shouldBe true
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
            val result = original.excludingUntaggedAuthors()

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
        fun `known gender count of zero returns empty gender slices rather than dividing by zero`() {
            // ----- Arrange -----
            val original = demographics(
                genderSlices = listOf(
                    GenderSlice(
                        gender = Gender.Unknown,
                        count = 5,
                        fraction = 1.0,
                    ),
                ),
                knownGenderCount = 0,
                unknownGenderCount = 5,
            )

            // ----- Act -----
            val result = original.excludingUntaggedAuthors()

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
            val result = original.excludingUntaggedAuthors()

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
                        gender = Gender.Male,
                        count = 4,
                        fraction = 0.5,
                    ),
                    GenderSlice(
                        gender = Gender.Female,
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
            val result = original.excludingUntaggedAuthors()

            // ----- Assert -----
            result shouldBe original
        }

        @Test
        fun `calling it on the default AuthorDemographics is safe`() {
            // ----- Arrange -----
            val original = AuthorDemographics()

            // ----- Act -----
            val result = original.excludingUntaggedAuthors()

            // ----- Assert -----
            result shouldBe AuthorDemographics()
        }
    }
}
