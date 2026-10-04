package nl.rhaydus.softcover.core.component.progress

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ProgressSheetUiModelTest {
    private fun timed(
        totalSeconds: Int,
        currentSeconds: Int,
    ): ProgressSheetMedium.Timed = ProgressSheetMedium.Timed(
        totalSeconds = totalSeconds,
        currentSeconds = currentSeconds,
        hours = progressTimeField(
            unit = ProgressTimeUnit.HOURS,
            text = "0",
        ),
        minutes = progressTimeField(
            unit = ProgressTimeUnit.MINUTES,
            text = "0",
        ),
        seconds = progressTimeField(
            unit = ProgressTimeUnit.SECONDS,
            text = "0",
        ),
    )

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
            val medium = timed(
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
            val medium = timed(
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
            val medium = timed(
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

    @Nested
    inner class Previews {
        @Test
        fun `fixtures include both a paged and a timed medium`() {
            // ----- Act -----
            val media = ProgressSheetUiModel.previews.map { it.medium }

            // ----- Assert -----
            media.any { it is ProgressSheetMedium.Paged } shouldBe true
            media.any { it is ProgressSheetMedium.Timed } shouldBe true
        }

        @Test
        fun `fixtures include a medium with no known total for each medium`() {
            // ----- Act -----
            val media = ProgressSheetUiModel.previews.map { it.medium }

            // ----- Assert -----
            media.any { it is ProgressSheetMedium.Paged && it.hasKnownTotal.not() } shouldBe true
            media.any { it is ProgressSheetMedium.Timed && it.hasKnownTotal.not() } shouldBe true
        }

        @Test
        fun `every fixture selects a tab its medium offers`() {
            // ----- Act -----
            val offered = ProgressSheetUiModel.previews.map { model ->
                model.tabs.options.any { it.key == model.selectedTab }
            }

            // ----- Assert -----
            offered.all { it } shouldBe true
        }
    }

    @Nested
    inner class ProgressTimeFieldBuilder {
        @Test
        fun `keys the field by the unit so events route back to it`() {
            // ----- Act -----
            val keys = ProgressTimeUnit.entries.map { unit ->
                progressTimeField(
                    unit = unit,
                    text = "1",
                ).key
            }

            // ----- Assert -----
            keys shouldBe listOf("HOURS", "MINUTES", "SECONDS")
        }

        @Test
        fun `carries the given text`() {
            // ----- Act -----
            val result = progressTimeField(
                unit = ProgressTimeUnit.MINUTES,
                text = "42",
            )

            // ----- Assert -----
            result.text shouldBe "42"
        }
    }

    @Nested
    inner class TimedMedium {
        @Test
        fun `has a known total only when totalSeconds is positive`() {
            // ----- Act -----
            val known = timed(
                totalSeconds = 1,
                currentSeconds = 0,
            )
            val unknown = timed(
                totalSeconds = 0,
                currentSeconds = 0,
            )

            // ----- Assert -----
            known.hasKnownTotal shouldBe true
            unknown.hasKnownTotal shouldBe false
        }
    }
}
