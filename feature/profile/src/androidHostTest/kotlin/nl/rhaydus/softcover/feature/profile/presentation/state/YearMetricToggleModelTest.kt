package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearMetricToggleModelTest {
    @Nested
    inner class YearMetricToggleFor {
        @Test
        fun `offers every metric in declaration order`() {
            // ----- Arrange -----
            val selected = YearMetric.BOOKS

            // ----- Act -----
            val result = yearMetricToggleFor(
                selected = selected,
                enabled = true,
            )

            // ----- Assert -----
            result.options.map { it.key } shouldBe YearMetric.entries.toList()
        }

        @Test
        fun `enables every option when enabled and keeps the selection`() {
            // ----- Arrange -----
            val selected = YearMetric.PAGES

            // ----- Act -----
            val result = yearMetricToggleFor(
                selected = selected,
                enabled = true,
            )

            // ----- Assert -----
            result.options.all { it.enabled } shouldBe true
            result.selected shouldBe YearMetric.PAGES
        }

        @Test
        fun `disables every option when not enabled but keeps the selection`() {
            // ----- Arrange -----
            val selected = YearMetric.PAGES

            // ----- Act -----
            val result = yearMetricToggleFor(
                selected = selected,
                enabled = false,
            )

            // ----- Assert -----
            result.options.none { it.enabled } shouldBe true
            result.selected shouldBe YearMetric.PAGES
        }
    }
}
