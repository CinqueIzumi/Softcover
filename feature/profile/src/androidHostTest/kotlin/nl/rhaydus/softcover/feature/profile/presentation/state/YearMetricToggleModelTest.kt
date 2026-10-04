package nl.rhaydus.softcover.feature.profile.presentation.state

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class YearMetricToggleModelTest {
    @Nested
    inner class YearMetricToggleFor {
        @Test
        fun `offers every metric in declaration order`() {
            // ----- Arrange & Act -----
            val result = yearMetricToggleFor(
                selected = YearMetric.BOOKS,
                enabled = true,
            )

            // ----- Assert -----
            result.options.map { it.key } shouldBe YearMetric.entries.toList()
        }

        @Test
        fun `labels each option with its metric label`() {
            // ----- Arrange & Act -----
            val result = yearMetricToggleFor(
                selected = YearMetric.BOOKS,
                enabled = true,
            )

            // ----- Assert -----
            result.options.map { it.label } shouldBe YearMetric.entries.map { it.label }
        }

        @Test
        fun `marks the given metric as selected`() {
            // ----- Arrange & Act -----
            val result = yearMetricToggleFor(
                selected = YearMetric.PAGES,
                enabled = true,
            )

            // ----- Assert -----
            result.selected shouldBe YearMetric.PAGES
        }

        @Test
        fun `enables every option when enabled`() {
            // ----- Arrange & Act -----
            val result = yearMetricToggleFor(
                selected = YearMetric.BOOKS,
                enabled = true,
            )

            // ----- Assert -----
            result.options.all { it.enabled } shouldBe true
        }

        @Test
        fun `disables every option when not enabled but keeps the selection`() {
            // ----- Arrange & Act -----
            val result = yearMetricToggleFor(
                selected = YearMetric.PAGES,
                enabled = false,
            )

            // ----- Assert -----
            result.options.none { it.enabled } shouldBe true
            result.selected shouldBe YearMetric.PAGES
        }
    }
}
