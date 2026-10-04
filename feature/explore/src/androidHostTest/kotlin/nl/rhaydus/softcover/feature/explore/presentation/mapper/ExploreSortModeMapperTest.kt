package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExploreSortModeMapperTest {
    @Nested
    inner class ToSortPillButton {
        @Test
        fun `labels the pill with the display label of the active mode`() {
            // ----- Arrange -----
            val labels = ExploreSortMode.entries.associateWith { mode ->
                (mode.toSortPillButton().content as PillButtonContent.Label).text
            }

            // ----- Act -----
            val expected = ExploreSortMode.entries.associateWith { mode -> "Sort · ${mode.displayLabel}" }

            // ----- Assert -----
            labels shouldBe expected
        }

        @Test
        fun `different modes produce different pills`() {
            // ----- Arrange -----
            val relevance = ExploreSortMode.RELEVANCE.toSortPillButton()

            // ----- Act -----
            val popularity = ExploreSortMode.POPULARITY.toSortPillButton()

            // ----- Assert -----
            (popularity == relevance) shouldBe false
        }

        @Test
        fun `keeps the same key across modes`() {
            // ----- Arrange -----
            val relevance = ExploreSortMode.RELEVANCE.toSortPillButton()

            // ----- Act -----
            val popularity = ExploreSortMode.POPULARITY.toSortPillButton()

            // ----- Assert -----
            popularity.key shouldBe relevance.key
        }
    }
}
