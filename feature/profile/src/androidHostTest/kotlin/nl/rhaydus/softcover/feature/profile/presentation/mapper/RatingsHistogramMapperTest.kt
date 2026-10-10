package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.component.statistic.StatNumberFormat
import nl.rhaydus.softcover.core.component.statistic.StatNumberVariant
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RatingsHistogramMapperTest {
    @Nested
    inner class RatingsChartUiModel {
        @Test
        fun `null ratings yield a loading model with nine columns`() {
            // ----- Arrange -----
            val ratings: RatingsDistribution? = null

            // ----- Act -----
            val result = ratingsChartUiModel(ratings)

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
            // ----- Arrange -----
            val ratings = RatingsDistribution()

            // ----- Act -----
            val columns = ratingsChartUiModel(ratings).columns

            // ----- Assert -----
            columns.map { it.fraction } shouldBe List(9) { 0f }
        }

        @Test
        fun `description of the first bucket is singular one star`() {
            // ----- Arrange -----
            val ratings = RatingsDistribution(halfStarBuckets = listOf(1, 0, 0, 0, 0, 0, 0, 0, 0))

            // ----- Act -----
            val columns = ratingsChartUiModel(ratings).columns

            // ----- Assert -----
            columns[0].contentDescription shouldBe "1 star, 1 book"
        }

        @Test
        fun `description of a half step carries the half and plural books`() {
            // ----- Arrange -----
            val ratings = RatingsDistribution(halfStarBuckets = listOf(0, 3, 0, 0, 0, 0, 0, 0, 0))

            // ----- Act -----
            val columns = ratingsChartUiModel(ratings).columns

            // ----- Assert -----
            columns[1].contentDescription shouldBe "1½ stars, 3 books"
        }

        @Test
        fun `description of the last bucket is five stars with plural zero books`() {
            // ----- Arrange -----
            val ratings = RatingsDistribution(halfStarBuckets = listOf(1, 0, 0, 0, 0, 0, 0, 0, 0))

            // ----- Act -----
            val columns = ratingsChartUiModel(ratings).columns

            // ----- Assert -----
            columns[8].contentDescription shouldBe "5 stars, 0 books"
        }
    }

    @Nested
    inner class RatingsAverageUiModel {
        @Test
        fun `null ratings yield a loading model`() {
            // ----- Arrange -----
            val ratings: RatingsDistribution? = null

            // ----- Act -----
            val result = ratingsAverageUiModel(ratings)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.variant shouldBe StatNumberVariant.Average
        }

        @Test
        fun `loaded ratings carry the average in decimal format`() {
            // ----- Arrange -----
            val ratings = RatingsDistribution(average = 4.2)

            // ----- Act -----
            val result = ratingsAverageUiModel(ratings)

            // ----- Assert -----
            result.isLoading shouldBe false
            result.value shouldBe 4.2
            result.format shouldBe StatNumberFormat.Decimal
            result.variant shouldBe StatNumberVariant.Average
        }
    }

    @Nested
    inner class ToRatingsHistogramHeader {
        @Test
        fun `every band gets its own headline`() {
            // ----- Arrange -----
            val bands = RatingBand.entries

            // ----- Act -----
            val headers = bands.map { it.toRatingsHistogramHeader() }

            // ----- Assert -----
            headers.map { it.headline }.distinct() shouldHaveSize bands.size
        }
    }

    @Nested
    inner class ToRatingsHistogramCaption {
        @Test
        fun `every band gets its own caption`() {
            // ----- Arrange -----
            val bands = RatingBand.entries

            // ----- Act -----
            val captions = bands.map { it.toRatingsHistogramCaption() }

            // ----- Assert -----
            captions.distinct() shouldHaveSize bands.size
        }
    }

    @Nested
    inner class RatingsAverageCaption {
        @Test
        fun `a single rating uses the singular unit`() {
            // ----- Arrange -----
            val totalRatings = 1

            // ----- Act -----
            val result = ratingsAverageCaption(totalRatings)

            // ----- Assert -----
            result shouldBe "AVERAGE ACROSS 1 BOOK"
        }

        @Test
        fun `zero ratings use the plural unit`() {
            // ----- Arrange -----
            val totalRatings = 0

            // ----- Act -----
            val result = ratingsAverageCaption(totalRatings)

            // ----- Assert -----
            result shouldBe "AVERAGE ACROSS 0 BOOKS"
        }

        @Test
        fun `many ratings use the plural unit`() {
            // ----- Arrange -----
            val totalRatings = 42

            // ----- Act -----
            val result = ratingsAverageCaption(totalRatings)

            // ----- Assert -----
            result shouldBe "AVERAGE ACROSS 42 BOOKS"
        }
    }
}
