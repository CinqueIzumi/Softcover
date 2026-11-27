package nl.rhaydus.softcover.core.component.chart

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.ranges.shouldBeIn
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ScoreBarsUiModelTest {
    @Nested
    inner class Previews {
        @Test
        fun `includes a loading fixture`() {
            // ----- Arrange -----
            val previews = ScoreBarsUiModel.previews.values

            // ----- Act -----
            val loading = previews.filter { it.isLoading }

            // ----- Assert -----
            loading.shouldNotBeEmpty()
        }

        @Test
        fun `keeps every fill within zero to one`() {
            // ----- Arrange -----
            val rows = ScoreBarsUiModel.previews.values.flatMap { it.rows }

            // ----- Act & Assert -----
            rows.forEach { it.fill shouldBeIn (0f..1f) }
        }
    }

    @Nested
    inner class Loading {
        @Test
        fun `yields the requested number of rows and marks it loading`() {
            // ----- Arrange -----
            val rowCount = 4

            // ----- Act -----
            val model = ScoreBarsUiModel.loading(rowCount = rowCount)

            // ----- Assert -----
            model.rows.shouldHaveSize(rowCount)
            model.isLoading shouldBe true
        }
    }
}
