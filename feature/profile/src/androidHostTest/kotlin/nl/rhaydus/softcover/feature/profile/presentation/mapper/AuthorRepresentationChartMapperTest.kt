package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.domain.model.Gender
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown
import nl.rhaydus.softcover.core.profile.domain.model.GenderSlice
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AuthorRepresentationChartMapperTest {
    private fun slice(
        gender: Gender,
        fraction: Double,
    ): GenderSlice = GenderSlice(
        gender = gender,
        count = 1,
        fraction = fraction,
    )

    @Nested
    inner class GenderChartUiModelTests {
        @Test
        fun `returns the loading chart with three masked segments when slices is null`() {
            // ----- Arrange -----
            val slices: List<GenderSlice>? = null

            // ----- Act -----
            val result = genderChartUiModel(slices)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.segments shouldHaveSize 3
            result.segments.map { it.share }.toSet() shouldBe setOf("")
        }

        @Test
        fun `returns no segments and is not loading for an empty list`() {
            // ----- Arrange -----
            val slices = emptyList<GenderSlice>()

            // ----- Act -----
            val result = genderChartUiModel(slices)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.segments shouldHaveSize 0
        }

        @Test
        fun `labels each gender and assigns Lead, Second, Third in rank order`() {
            // ----- Arrange -----
            val slices = listOf(
                slice(
                    gender = Gender.Female,
                    fraction = 0.5,
                ),
                slice(
                    gender = Gender.Male,
                    fraction = 0.3,
                ),
                slice(
                    gender = Gender.Other,
                    fraction = 0.2,
                ),
            )

            // ----- Act -----
            val result = genderChartUiModel(slices)

            // ----- Assert -----
            result.segments.map { it.label } shouldBe listOf("Women", "Men", "Other")
            result.segments.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Second, ChartTone.Third)
            result.segments.map { it.share } shouldBe listOf("50%", "30%", "20%")
            result.segments.map { it.fraction } shouldBe listOf(0.5f, 0.3f, 0.2f)
        }

        @Test
        fun `maps Unknown to the Unknown tone without consuming a known tone`() {
            // ----- Arrange -----
            val slices = listOf(
                slice(
                    gender = Gender.Male,
                    fraction = 0.4,
                ),
                slice(
                    gender = Gender.Unknown,
                    fraction = 0.35,
                ),
                slice(
                    gender = Gender.Female,
                    fraction = 0.25,
                ),
            )

            // ----- Act -----
            val result = genderChartUiModel(slices)

            // ----- Assert -----
            result.segments.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Unknown, ChartTone.Second)
            result.segments[1].label shouldBe "Unknown"
        }
    }

    @Nested
    inner class DemographicChartUiModelTests {
        @Test
        fun `returns the loading chart with three masked segments when breakdown is null`() {
            // ----- Arrange -----
            val breakdown: DemographicBreakdown? = null

            // ----- Act -----
            val result = demographicChartUiModel(breakdown)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.segments shouldHaveSize 3
        }

        @Test
        fun `maps Yes to Lead and No to Fourth and omits Unknown when nobody is untagged`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown(
                yesCount = 1,
                noCount = 3,
                unknownCount = 0,
            )

            // ----- Act -----
            val result = demographicChartUiModel(breakdown)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.segments.map { it.label } shouldBe listOf("Yes", "No")
            result.segments.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Fourth)
            result.segments.map { it.share } shouldBe listOf("25%", "75%")
            result.segments.map { it.fraction } shouldBe listOf(0.25f, 0.75f)
        }

        @Test
        fun `adds an Unknown segment when unknownCount is positive`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown(
                yesCount = 2,
                noCount = 2,
                unknownCount = 4,
            )

            // ----- Act -----
            val result = demographicChartUiModel(breakdown)

            // ----- Assert -----
            result.segments.map { it.label } shouldBe listOf("Yes", "No", "Unknown")
            result.segments.last().tone shouldBe ChartTone.Unknown
            result.segments.map { it.share } shouldBe listOf("25%", "25%", "50%")
        }

        @Test
        fun `keeps Yes and No at 0 percent when there is no data at all`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown()

            // ----- Act -----
            val result = demographicChartUiModel(breakdown)

            // ----- Assert -----
            result.segments.map { it.label } shouldBe listOf("Yes", "No")
            result.segments.map { it.share } shouldBe listOf("0%", "0%")
            result.segments.map { it.fraction } shouldBe listOf(0f, 0f)
        }

        @Test
        fun `keeps Yes at 0 percent when only No is present`() {
            // ----- Arrange -----
            val breakdown = DemographicBreakdown(
                yesCount = 0,
                noCount = 5,
            )

            // ----- Act -----
            val result = demographicChartUiModel(breakdown)

            // ----- Assert -----
            result.segments.map { it.share } shouldBe listOf("0%", "100%")
        }
    }
}
