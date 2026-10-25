package nl.rhaydus.softcover.core.component.chart

import io.kotest.matchers.collections.shouldNotBeEmpty
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
    }
}
