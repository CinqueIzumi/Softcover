package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.chart.ChartTone
import nl.rhaydus.softcover.core.profile.domain.model.YearCount
import nl.rhaydus.softcover.feature.profile.presentation.state.YearMetric
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearHistoryMapperTest {
    private val fallbackCaption = yearHistoryCaption(
        series = null,
        metric = YearMetric.BOOKS,
    )

    private fun chartOf(
        series: List<YearCount>?,
        metric: YearMetric = YearMetric.BOOKS,
    ) = yearHistoryChartUiModel(
        series = series,
        metric = metric,
    )

    @Nested
    inner class YearHistoryChartUiModel {
        @Test
        fun `null series yields a loading model with eight columns`() {
            // ----- Arrange & Act -----
            val result = chartOf(null)

            // ----- Assert -----
            result.isLoading shouldBe true
            result.columns shouldHaveSize 8
        }

        @Test
        fun `loaded series is not loading`() {
            // ----- Arrange & Act -----
            val result = chartOf(
                listOf(
                    YearCount(
                        year = 2024,
                        count = 3,
                    ),
                ),
            )

            // ----- Assert -----
            result.isLoading shouldBe false
        }

        @Test
        fun `peak column is Lead, other non-zero Second and zero Fifth without a value`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2022,
                    count = 5,
                ),
                YearCount(
                    year = 2023,
                    count = 10,
                ),
                YearCount(
                    year = 2024,
                    count = 0,
                ),
            )

            // ----- Act -----
            val columns = chartOf(series).columns

            // ----- Assert -----
            columns.map { it.tone } shouldBe listOf(ChartTone.Second, ChartTone.Lead, ChartTone.Fifth)
            columns[2].value shouldBe null
            columns[1].value shouldBe "10"
        }

        @Test
        fun `first max wins on a tie`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2022,
                    count = 7,
                ),
                YearCount(
                    year = 2023,
                    count = 7,
                ),
            )

            // ----- Act -----
            val columns = chartOf(series).columns

            // ----- Assert -----
            columns.map { it.tone } shouldBe listOf(ChartTone.Lead, ChartTone.Second)
        }

        @Test
        fun `all-zero series has no Lead column`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2022,
                    count = 0,
                ),
                YearCount(
                    year = 2023,
                    count = 0,
                ),
            )

            // ----- Act -----
            val columns = chartOf(series).columns

            // ----- Assert -----
            columns.map { it.tone } shouldBe listOf(ChartTone.Fifth, ChartTone.Fifth)
            columns.map { it.fraction } shouldBe listOf(0f, 0f)
        }

        @Test
        fun `empty series yields no columns`() {
            // ----- Arrange & Act -----
            val result = chartOf(emptyList())

            // ----- Assert -----
            result.columns shouldHaveSize 0
            result.isLoading shouldBe false
        }

        @Test
        fun `fraction is count divided by the maximum`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2022,
                    count = 5,
                ),
                YearCount(
                    year = 2023,
                    count = 20,
                ),
            )

            // ----- Act -----
            val columns = chartOf(series).columns

            // ----- Assert -----
            columns.map { it.fraction } shouldBe listOf(0.25f, 1f)
        }

        @Test
        fun `label is an apostrophe and the zero-padded two-digit year`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2005,
                    count = 1,
                ),
                YearCount(
                    year = 2024,
                    count = 1,
                ),
                YearCount(
                    year = 1999,
                    count = 1,
                ),
            )

            // ----- Act -----
            val columns = chartOf(series).columns

            // ----- Assert -----
            columns.map { it.label } shouldBe listOf("'05", "'24", "'99")
        }

        @Test
        fun `value stays plain at 999`() {
            // ----- Arrange & Act -----
            val result = chartOf(
                listOf(
                    YearCount(
                        year = 2024,
                        count = 999,
                    ),
                ),
            )

            // ----- Assert -----
            result.columns.single().value shouldBe "999"
        }

        @Test
        fun `value becomes thousands at 1000`() {
            // ----- Arrange & Act -----
            val result = chartOf(
                listOf(
                    YearCount(
                        year = 2024,
                        count = 1_000,
                    ),
                ),
            )

            // ----- Assert -----
            result.columns.single().value shouldBe "1.0k"
        }

        @Test
        fun `value stays in thousands at 999949`() {
            // ----- Arrange & Act -----
            val result = chartOf(
                listOf(
                    YearCount(
                        year = 2024,
                        count = 999_949,
                    ),
                ),
            )

            // ----- Assert -----
            result.columns.single().value shouldBe "999.9k"
        }

        @Test
        fun `value rolls over to millions at 999999`() {
            // ----- Arrange & Act -----
            val result = chartOf(
                listOf(
                    YearCount(
                        year = 2024,
                        count = 999_999,
                    ),
                ),
            )

            // ----- Assert -----
            result.columns.single().value shouldBe "1.0M"
        }

        @Test
        fun `value becomes millions at 1000000`() {
            // ----- Arrange & Act -----
            val result = chartOf(
                listOf(
                    YearCount(
                        year = 2024,
                        count = 1_000_000,
                    ),
                ),
            )

            // ----- Assert -----
            result.columns.single().value shouldBe "1.0M"
        }

        @Test
        fun `description names the year and the books count`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2023,
                    count = 12,
                ),
            )

            // ----- Act -----
            val columns = chartOf(
                series = series,
                metric = YearMetric.BOOKS,
            ).columns

            // ----- Assert -----
            columns.single().contentDescription shouldBe "2023, 12 books"
        }

        @Test
        fun `description uses the singular unit for a count of one`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2023,
                    count = 1,
                ),
            )

            // ----- Act -----
            val books = chartOf(
                series = series,
                metric = YearMetric.BOOKS,
            ).columns
            val pages = chartOf(
                series = series,
                metric = YearMetric.PAGES,
            ).columns

            // ----- Assert -----
            books.single().contentDescription shouldBe "2023, 1 book"
            pages.single().contentDescription shouldBe "2023, 1 page"
        }

        @Test
        fun `description uses grouped pages for the pages metric`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2021,
                    count = 12_345,
                ),
            )

            // ----- Act -----
            val columns = chartOf(
                series = series,
                metric = YearMetric.PAGES,
            ).columns

            // ----- Assert -----
            columns.single().contentDescription shouldBe "2021, ${formatGroupedNumber(12_345)} pages"
        }

        @Test
        fun `description of a zero year is plural`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2020,
                    count = 0,
                ),
            )

            // ----- Act -----
            val columns = chartOf(series).columns

            // ----- Assert -----
            columns.single().contentDescription shouldBe "2020, 0 books"
        }

        @Test
        fun `loading columns have an empty description`() {
            // ----- Arrange & Act -----
            val columns = chartOf(null).columns

            // ----- Assert -----
            columns.map { it.contentDescription }.distinct() shouldBe listOf("")
        }

        @Test
        fun `values are shown when loaded, loading and all-zero`() {
            // ----- Arrange -----
            val allZero = listOf(
                YearCount(
                    year = 2023,
                    count = 0,
                ),
            )
            val loaded = listOf(
                YearCount(
                    year = 2023,
                    count = 4,
                ),
            )

            // ----- Act -----
            val loadedChart = chartOf(loaded)
            val loadingChart = chartOf(null)
            val allZeroChart = chartOf(allZero)

            // ----- Assert -----
            loadedChart.showsValues shouldBe true
            loadingChart.showsValues shouldBe true
            allZeroChart.showsValues shouldBe true
        }
    }

    @Nested
    inner class YearHistoryCaption {
        @Test
        fun `names the peak year and the books count`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2022,
                    count = 5,
                ),
                YearCount(
                    year = 2023,
                    count = 12,
                ),
            )

            // ----- Act -----
            val result = yearHistoryCaption(
                series = series,
                metric = YearMetric.BOOKS,
            )

            // ----- Assert -----
            result shouldBe "2023 was your busiest year yet — 12 books."
        }

        @Test
        fun `names the pages unit with a grouped number`() {
            // ----- Arrange -----
            val series = listOf(
                YearCount(
                    year = 2021,
                    count = 12_345,
                ),
            )

            // ----- Act -----
            val result = yearHistoryCaption(
                series = series,
                metric = YearMetric.PAGES,
            )

            // ----- Assert -----
            result shouldBe "2021 was your busiest year yet — ${formatGroupedNumber(12_345)} pages."
        }

        @Test
        fun `empty series returns the fallback copy`() {
            // ----- Arrange & Act -----
            val result = yearHistoryCaption(
                series = emptyList(),
                metric = YearMetric.BOOKS,
            )

            // ----- Assert -----
            result shouldBe fallbackCaption
        }

        @Test
        fun `all-zero series returns the fallback copy`() {
            // ----- Arrange & Act -----
            val result = yearHistoryCaption(
                series = listOf(
                    YearCount(
                        year = 2023,
                        count = 0,
                    ),
                ),
                metric = YearMetric.PAGES,
            )

            // ----- Assert -----
            result shouldBe fallbackCaption
        }

        @Test
        fun `null series returns the fallback copy for either metric`() {
            // ----- Arrange & Act -----
            val result = yearHistoryCaption(
                series = null,
                metric = YearMetric.PAGES,
            )

            // ----- Assert -----
            result shouldBe fallbackCaption
        }
    }
}
