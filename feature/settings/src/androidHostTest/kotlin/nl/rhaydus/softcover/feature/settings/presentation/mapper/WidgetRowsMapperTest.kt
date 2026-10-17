package nl.rhaydus.softcover.feature.settings.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeBlank
import nl.rhaydus.softcover.core.presentation.widget.HomeScreenWidgetKind
import nl.rhaydus.softcover.core.presentation.widget.PlacedHomeScreenWidget
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class WidgetRowsMapperTest {
    @Nested
    inner class PlacedRows {
        @Test
        fun `uses the summary as gloss when present`() {
            // ----- Arrange -----
            val widgets = listOf(
                PlacedHomeScreenWidget(
                    id = 1,
                    kind = HomeScreenWidgetKind.RandomPick,
                    summary = "Want to read",
                    hasSettings = true,
                ),
            )

            // ----- Act -----
            val result = placedWidgetRowsFor(widgets = widgets)

            // ----- Assert -----
            result.single().gloss shouldBe "Want to read"
        }

        @Test
        fun `falls back to one shared gloss for every widget without a summary`() {
            // ----- Arrange -----
            val widgets = listOf(
                PlacedHomeScreenWidget(
                    id = 1,
                    kind = HomeScreenWidgetKind.Trending,
                    hasSettings = false,
                ),
                PlacedHomeScreenWidget(
                    id = 2,
                    kind = HomeScreenWidgetKind.RandomPick,
                    hasSettings = false,
                ),
            )

            // ----- Act -----
            val result = placedWidgetRowsFor(widgets = widgets)

            // ----- Assert -----
            result[0].gloss.shouldNotBeBlank()
            result[0].gloss shouldBe result[1].gloss
        }
    }
}
