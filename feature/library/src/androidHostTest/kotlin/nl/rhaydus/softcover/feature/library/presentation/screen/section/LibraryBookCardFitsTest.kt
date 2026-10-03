package nl.rhaydus.softcover.feature.library.presentation.screen.section

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryBookCardFitsTest {
    private fun card(variant: BookCardVariant): BookCardUiModel = BookCardUiModel(
        key = BookCardKey.Book(bookId = 1),
        variant = variant,
    )

    private fun rowCard(size: BookCardVariant.Row.Size): BookCardUiModel = card(
        variant = BookCardVariant.Row(
            size = size,
            lead = null,
            text = mockk<BookCardContent>(relaxed = true),
        ),
    )

    private fun fittingLayouts(model: BookCardUiModel): Set<LibraryGridLayout> =
        LibraryGridLayout.entries.filter { layout -> model.fits(layout) }.toSet()

    @Nested
    inner class Fits {
        @Test
        fun `a cover-only card fits only the cover-only grids`() {
            // ----- Arrange -----
            val model = card(variant = BookCardVariant.CoverOnly(cover = null))

            // ----- Act -----
            val result = fittingLayouts(model)

            // ----- Assert -----
            result shouldBe setOf(
                LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
                LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
            )
        }

        @Test
        fun `a stacked card fits only the titled grids`() {
            // ----- Arrange -----
            val model = card(
                variant = BookCardVariant.Stacked(
                    cover = null,
                    text = mockk<BookCardContent>(relaxed = true),
                ),
            )

            // ----- Act -----
            val result = fittingLayouts(model)

            // ----- Assert -----
            result shouldBe setOf(
                LibraryGridLayout.GRID_TWO_COLUMNS,
                LibraryGridLayout.GRID_THREE_COLUMNS,
            )
        }

        @Test
        fun `a compact row fits only the compact list`() {
            // ----- Arrange -----
            val model = rowCard(size = BookCardVariant.Row.Size.Compact)

            // ----- Act -----
            val result = fittingLayouts(model)

            // ----- Assert -----
            result shouldBe setOf(LibraryGridLayout.LIST_COMPACT)
        }

        @Test
        fun `a large row fits only the large list`() {
            // ----- Arrange -----
            val model = rowCard(size = BookCardVariant.Row.Size.Large)

            // ----- Act -----
            val result = fittingLayouts(model)

            // ----- Assert -----
            result shouldBe setOf(LibraryGridLayout.LIST_LARGE)
        }

        @Test
        fun `a standard row fits no layout`() {
            // ----- Arrange -----
            val model = rowCard(size = BookCardVariant.Row.Size.Standard)

            // ----- Act -----
            val result = fittingLayouts(model)

            // ----- Assert -----
            result shouldBe emptySet()
        }
    }
}
