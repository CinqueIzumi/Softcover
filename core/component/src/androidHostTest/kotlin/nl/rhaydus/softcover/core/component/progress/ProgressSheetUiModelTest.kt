package nl.rhaydus.softcover.core.component.progress

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressSheetUiModelTest {
    @Nested
    inner class TabsFor {
        @Test
        fun `offers page and percentage for a paged book with a known total`() {
            // ----- Arrange -----
            val medium = ProgressSheetMedium.Paged(
                totalPages = 300,
                currentPage = 10,
            )

            // ----- Act -----
            val result = ProgressSheetUiModel.tabsFor(
                medium = medium,
                selectedTab = ProgressSheetTab.PAGE,
            )

            // ----- Assert -----
            result.options.map { it.key } shouldBe listOf(
                ProgressSheetTab.PAGE,
                ProgressSheetTab.PERCENTAGE,
            )
        }

        @Test
        fun `offers only page for a paged book without a total`() {
            // ----- Arrange -----
            val medium = ProgressSheetMedium.Paged(
                totalPages = 0,
                currentPage = 0,
            )

            // ----- Act -----
            val result = ProgressSheetUiModel.tabsFor(
                medium = medium,
                selectedTab = ProgressSheetTab.PAGE,
            )

            // ----- Assert -----
            result.options.map { it.key } shouldBe listOf(ProgressSheetTab.PAGE)
        }

        @Test
        fun `offers time and percentage for an audiobook with a known total`() {
            // ----- Arrange -----
            val medium = ProgressSheetMedium.Timed(
                totalSeconds = 36_000,
                currentSeconds = 600,
            )

            // ----- Act -----
            val result = ProgressSheetUiModel.tabsFor(
                medium = medium,
                selectedTab = ProgressSheetTab.TIME,
            )

            // ----- Assert -----
            result.options.map { it.key } shouldBe listOf(
                ProgressSheetTab.TIME,
                ProgressSheetTab.PERCENTAGE,
            )
        }

        @Test
        fun `offers only time for an audiobook without a total`() {
            // ----- Arrange -----
            val medium = ProgressSheetMedium.Timed(
                totalSeconds = 0,
                currentSeconds = 0,
            )

            // ----- Act -----
            val result = ProgressSheetUiModel.tabsFor(
                medium = medium,
                selectedTab = ProgressSheetTab.TIME,
            )

            // ----- Assert -----
            result.options.map { it.key } shouldBe listOf(ProgressSheetTab.TIME)
        }

        @Test
        fun `keeps the selected tab when it is offered`() {
            // ----- Arrange -----
            val medium = ProgressSheetMedium.Paged(
                totalPages = 300,
                currentPage = 10,
            )

            // ----- Act -----
            val result = ProgressSheetUiModel.tabsFor(
                medium = medium,
                selectedTab = ProgressSheetTab.PERCENTAGE,
            )

            // ----- Assert -----
            result.selected shouldBe ProgressSheetTab.PERCENTAGE
        }

        @Test
        fun `falls back to page when percentage is selected but no total is known`() {
            // ----- Arrange -----
            val medium = ProgressSheetMedium.Paged(
                totalPages = 0,
                currentPage = 0,
            )

            // ----- Act -----
            val result = ProgressSheetUiModel.tabsFor(
                medium = medium,
                selectedTab = ProgressSheetTab.PERCENTAGE,
            )

            // ----- Assert -----
            result.selected shouldBe ProgressSheetTab.PAGE
        }

        @Test
        fun `falls back to time when a stored page tab meets an audiobook`() {
            // ----- Arrange -----
            val medium = ProgressSheetMedium.Timed(
                totalSeconds = 36_000,
                currentSeconds = 600,
            )

            // ----- Act -----
            val result = ProgressSheetUiModel.tabsFor(
                medium = medium,
                selectedTab = ProgressSheetTab.PAGE,
            )

            // ----- Assert -----
            result.selected shouldBe ProgressSheetTab.TIME
        }

        @Test
        fun `labels each option with its tab name`() {
            // ----- Arrange -----
            val medium = ProgressSheetMedium.Paged(
                totalPages = 300,
                currentPage = 10,
            )

            // ----- Act -----
            val result = ProgressSheetUiModel.tabsFor(
                medium = medium,
                selectedTab = ProgressSheetTab.PAGE,
            )

            // ----- Assert -----
            result.options.map { it.label } shouldBe listOf(
                ProgressSheetTab.PAGE.tabName,
                ProgressSheetTab.PERCENTAGE.tabName,
            )
        }

        @Test
        fun `exposes the selected tab on the model`() {
            // ----- Arrange -----
            val medium = ProgressSheetMedium.Paged(
                totalPages = 300,
                currentPage = 10,
            )
            val tabs = ProgressSheetUiModel.tabsFor(
                medium = medium,
                selectedTab = ProgressSheetTab.PERCENTAGE,
            )

            // ----- Act -----
            val model = ProgressSheetUiModel.previews.first().copy(
                medium = medium,
                tabs = tabs,
            )

            // ----- Assert -----
            model.selectedTab shouldBe ProgressSheetTab.PERCENTAGE
        }
    }
}
