package nl.rhaydus.softcover.core.component.chart

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class RankedBarsUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `includes a loading fixture`() {
            // ----- Arrange -----
            val previews = RankedBarsUiModel.previews.values

            // ----- Act -----
            val loading = previews.filter { it.isLoading }

            // ----- Assert -----
            loading.shouldNotBeEmpty()
        }

        @Test
        fun `every non-loading tone Lead to Fifth appears in a data fixture`() {
            // ----- Arrange -----
            val expectedTones = ChartTone.entries.filter { it != ChartTone.Unknown }.toSet()

            // ----- Act -----
            val coveredTones = RankedBarsUiModel.previews.values
                .filter { it.isLoading.not() }
                .flatMap { it.rows }
                .map { it.tone }
                .toSet()

            // ----- Assert -----
            coveredTones shouldBe expectedTones
        }
    }
}
