package nl.rhaydus.softcover.feature.library.presentation.screen.section

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryStandInsTest {
    @Nested
    inner class StandInCard {
        @Test
        fun `every layout gets a stand-in of the variant that layout draws`() {
            // ----- Arrange -----
            val expected = mapOf(
                LibraryGridLayout.GRID_TWO_COLUMNS to BookCardVariant.Column::class,
                LibraryGridLayout.GRID_THREE_COLUMNS to BookCardVariant.Column::class,
                LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY to BookCardVariant.CoverOnly::class,
                LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY to BookCardVariant.CoverOnly::class,
                LibraryGridLayout.LIST_COMPACT to BookCardVariant.Row::class,
                LibraryGridLayout.LIST_LARGE to BookCardVariant.Row::class,
            )

            // ----- Act & Assert -----
            LibraryGridLayout.entries.forEach { layout ->
                layout.standInCard().variant::class shouldBe expected.getValue(layout)
            }
        }

        @Test
        fun `list layouts get a row of the matching size`() {
            // ----- Act & Assert -----
            LibraryGridLayout.LIST_COMPACT.standInCard().variant
                .shouldBeInstanceOf<BookCardVariant.Row>().size shouldBe BookCardVariant.Row.Size.Compact
            LibraryGridLayout.LIST_LARGE.standInCard().variant
                .shouldBeInstanceOf<BookCardVariant.Row>().size shouldBe BookCardVariant.Row.Size.Large
        }

        @Test
        fun `every stand-in is inert`() {
            // ----- Act & Assert -----
            LibraryGridLayout.entries.forEach { layout ->
                layout.standInCard().decorations.pressBehaviour shouldBe BookCardDecorations.Press.None
            }
        }
    }
}
