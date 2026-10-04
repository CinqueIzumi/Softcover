package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RatingsHistogramMapperTest {
    @Nested
    inner class RatingsChartUiModel {
        @Test
        fun `null ratings yield a loading model with nine columns`() {
            // ----- Arrange & Act -----
            val result = ratingsChartUiModel(null)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.columns shouldHaveSize 9
        }

        @Test
        fun `whole steps are labelled and Lead, half steps unlabelled and Fourth`() {
            // ----- Arrange -----
            val ratings = RatingsDistribution(halfStarBuckets = List(9) { 4 })

            // ----- Act -----
            val result = ratingsChartUiModel(ratings)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.columns.map { it.label } shouldBe
                listOf("1★", null, "2★", null, "3★", null, "4★", null, "5★")
            result.columns.map { it.tone } shouldBe List(9) { index ->
                if (index % 2 == 0) ChartTone.Lead else ChartTone.Fourth
            }
        }

        @Test
        fun `zero count column is Fifth with no fraction`() {
            // ----- Arrange -----
            val ratings = RatingsDistribution(
                halfStarBuckets = listOf(0, 0, 2, 0, 0, 0, 0, 0, 4),
            )

            // ----- Act -----
            val columns = ratingsChartUiModel(ratings).columns

            // ----- Assert -----
            columns[0].tone shouldBe ChartTone.Fifth
            columns[1].tone shouldBe ChartTone.Fifth
            columns[0].fraction shouldBe 0f
            columns[2].tone shouldBe ChartTone.Lead
        }

        @Test
        fun `fraction is the count relative to the largest bucket`() {
            // ----- Arrange -----
            val ratings = RatingsDistribution(
                halfStarBuckets = listOf(1, 0, 2, 0, 0, 0, 0, 0, 4),
            )

            // ----- Act -----
            val columns = ratingsChartUiModel(ratings).columns

            // ----- Assert -----
            columns[0].fraction shouldBe 0.25f
            columns[2].fraction shouldBe 0.5f
            columns[8].fraction shouldBe 1f
        }

        @Test
        fun `all-zero buckets give zero fractions without dividing by zero`() {
            // ----- Arrange & Act -----
            val columns = ratingsChartUiModel(RatingsDistribution()).columns

            // ----- Assert -----
            columns.map { it.fraction } shouldBe List(9) { 0f }
        }

        @Test
        fun `columns never carry a value`() {
            // ----- Arrange -----
            val ratings = RatingsDistribution(halfStarBuckets = List(9) { 3 })

            // ----- Act -----
            val loaded = ratingsChartUiModel(ratings).columns
            val loading = ratingsChartUiModel(null).columns

            // ----- Assert -----
            loaded.map { it.value }.distinct() shouldBe listOf(null)
            loading.map { it.value }.distinct() shouldBe listOf(null)
        }
    }

    @Nested
    inner class RatingsAverageUiModel {
        @Test
        fun `null ratings yield a loading model`() {
            // ----- Arrange & Act -----
            val result = ratingsAverageUiModel(null)

            // ----- Assert -----
            result.isLoading shouldBe true
        }

        @Test
        fun `loaded ratings carry the average in decimal format`() {
            // ----- Arrange & Act -----
            val result = ratingsAverageUiModel(RatingsDistribution(average = 4.2))

            // ----- Assert -----
            result.isLoading shouldBe false
            result.value shouldBe 4.2
            result.format shouldBe StatNumberFormat.Decimal(fractionDigits = 1)
        }
    }

    @Nested
    inner class ToRatingsHistogramHeader {
        @Test
        fun `GENEROUS band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.GENEROUS.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "A generous but honest hand",
            )
        }

        @Test
        fun `SUPERFAN band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.SUPERFAN.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "Easily, happily impressed",
            )
        }

        @Test
        fun `CENTRIST band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.CENTRIST.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "You keep to the middle of the shelf",
            )
        }

        @Test
        fun `EXACTING band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.EXACTING.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "A hard star to earn",
            )
        }

        @Test
        fun `POLARISED band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.POLARISED.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "Love it or leave it",
            )
        }

        @Test
        fun `NEW_READER band header carries the shared eyebrow and its own headline`() {
            // ----- Arrange & Act -----
            val result = RatingBand.NEW_READER.toRatingsHistogramHeader()

            // ----- Assert -----
            result shouldBe SectionHeaderUiModel.Section(
                eyebrow = "How you score",
                headline = "Still finding your scale",
            )
        }

        @Test
        fun `header never carries a description`() {
            // ----- Arrange & Act -----
            val result = RatingBand.CENTRIST.toRatingsHistogramHeader()

            // ----- Assert -----
            result.description shouldBe null
        }
    }

    @Nested
    inner class ToRatingsHistogramCaption {
        @Test
        fun `GENEROUS band caption describes the half-star rounding pattern`() {
            // ----- Arrange & Act -----
            val result = RatingBand.GENEROUS.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Half-stars count. Most of yours land between four and five — you round up " +
                "when a book earns it."
        }

        @Test
        fun `SUPERFAN band caption describes the above four-and-a-half average`() {
            // ----- Arrange & Act -----
            val result = RatingBand.SUPERFAN.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Your average sits north of four and a half — you finish what you start, " +
                "and it usually earns the top shelf."
        }

        @Test
        fun `CENTRIST band caption describes the clustering around three and a half`() {
            // ----- Arrange & Act -----
            val result = RatingBand.CENTRIST.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Your ratings cluster around three and a half — steady and considered, " +
                "rarely swinging to an extreme."
        }

        @Test
        fun `EXACTING band caption describes the low average`() {
            // ----- Arrange & Act -----
            val result = RatingBand.EXACTING.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Your average runs low — a five-star read is rare here, which makes the " +
                "ones you give out mean something."
        }

        @Test
        fun `POLARISED band caption describes the two-camp split`() {
            // ----- Arrange & Act -----
            val result = RatingBand.POLARISED.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Your ratings split into two camps, low and high, with little in between " +
                "— a book either wins you over or it doesn't."
        }

        @Test
        fun `NEW_READER band caption asks for more ratings`() {
            // ----- Arrange & Act -----
            val result = RatingBand.NEW_READER.toRatingsHistogramCaption()

            // ----- Assert -----
            result shouldBe "Not enough ratings yet to say much about your taste — a few more books " +
                "will fill this in."
        }
    }
}
