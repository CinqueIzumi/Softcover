package nl.rhaydus.softcover.core.component.chart

import io.kotest.matchers.collections.shouldNotBeEmpty
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProportionChartUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `includes a loading fixture`() {
            // ----- Arrange -----
            val previews = ProportionChartUiModel.previews.values

            // ----- Act -----
            val loading = previews.filter { it.isLoading }

            // ----- Assert -----
            loading.shouldNotBeEmpty()
        }

        @Test
        fun `includes a fixture with an Unknown-toned segment`() {
            // ----- Arrange -----
            val previews = ProportionChartUiModel.previews.values

            // ----- Act -----
            val withUnknown = previews
                .filter { it.isLoading.not() }
                .filter { chart -> chart.segments.any { it.tone == ChartTone.Unknown } }

            // ----- Assert -----
            withUnknown.shouldNotBeEmpty()
        }

        @Test
        fun `includes a fixture with a zero-fraction segment`() {
            // ----- Arrange -----
            val previews = ProportionChartUiModel.previews.values

            // ----- Act -----
            val withZero = previews
                .filter { it.isLoading.not() }
                .filter { chart -> chart.segments.any { it.fraction == 0f } }

            // ----- Assert -----
            withZero.shouldNotBeEmpty()
        }
    }
}
