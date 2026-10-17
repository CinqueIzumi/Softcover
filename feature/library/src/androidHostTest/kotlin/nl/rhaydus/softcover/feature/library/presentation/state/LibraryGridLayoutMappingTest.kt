package nl.rhaydus.softcover.feature.library.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryGridLayoutMappingTest {
    @Nested
    inner class ToLayout {
        @Test
        fun `every layout except the compact list round-trips through its chip and titled-ness`() {
            // ----- Arrange -----
            val layouts = LibraryGridLayout.entries.filterNot { it == LibraryGridLayout.LIST_COMPACT }

            // ----- Act -----
            val roundTripped = layouts.map { it.chip.toLayout(showTitles = it.showsTitles) }

            // ----- Assert -----
            roundTripped shouldBe layouts
        }

        @Test
        fun `the compact list collapses to the large list because the picker does not offer it`() {
            // ----- Arrange -----
            val layout = LibraryGridLayout.LIST_COMPACT

            // ----- Act -----
            val result = layout.chip.toLayout(showTitles = layout.showsTitles)

            // ----- Assert -----
            result shouldBe LibraryGridLayout.LIST_LARGE
        }

        @Test
        fun `the list chip ignores whether titles are shown because it has no cover-only sibling`() {
            // ----- Arrange -----
            val chip = LibraryLayoutChip.LIST

            // ----- Act -----
            val shown = chip.toLayout(showTitles = true)
            val hidden = chip.toLayout(showTitles = false)

            // ----- Assert -----
            shown shouldBe LibraryGridLayout.LIST_LARGE
            hidden shouldBe LibraryGridLayout.LIST_LARGE
        }
    }

    @Nested
    inner class WithTitlesShown {
        @Test
        fun `hiding titles on a grid layout switches to its cover-only sibling in the same column count`() {
            // ----- Arrange -----
            val grids = listOf(
                LibraryGridLayout.GRID_TWO_COLUMNS,
                LibraryGridLayout.GRID_THREE_COLUMNS,
            )

            // ----- Act -----
            val result = grids.map { it.withTitlesShown(show = false) }

            // ----- Assert -----
            result shouldBe listOf(
                LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
                LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            )
        }

        @Test
        fun `showing titles on a cover-only grid layout switches back to its titled sibling`() {
            // ----- Arrange -----
            val grids = listOf(
                LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
                LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            )

            // ----- Act -----
            val result = grids.map { it.withTitlesShown(show = true) }

            // ----- Assert -----
            result shouldBe listOf(
                LibraryGridLayout.GRID_TWO_COLUMNS,
                LibraryGridLayout.GRID_THREE_COLUMNS,
            )
        }

        @Test
        fun `requesting the titled-ness a layout already has leaves it unchanged`() {
            // ----- Arrange -----
            val layouts = LibraryGridLayout.entries

            // ----- Act -----
            val result = layouts.map { it.withTitlesShown(show = it.showsTitles) }

            // ----- Assert -----
            result shouldBe LibraryGridLayout.entries
        }

        @Test
        fun `list layouts are unchanged whether titles are shown or hidden`() {
            // ----- Arrange -----
            val lists = listOf(LibraryGridLayout.LIST_COMPACT, LibraryGridLayout.LIST_LARGE)

            // ----- Act -----
            val shown = lists.map { it.withTitlesShown(show = true) }
            val hidden = lists.map { it.withTitlesShown(show = false) }

            // ----- Assert -----
            shown shouldBe lists
            hidden shouldBe lists
        }
    }
}
