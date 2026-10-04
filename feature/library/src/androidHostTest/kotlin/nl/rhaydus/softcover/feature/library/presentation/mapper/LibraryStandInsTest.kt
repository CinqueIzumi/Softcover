package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryStandInsTest {
    @Nested
    inner class StandInCards {
        @Test
        fun `the cards of a layout have distinct keys`() {
            // ----- Act & Assert -----
            LibraryGridLayout.entries.forEach { layout ->
                val keys = layout.standInCards().map { it.key }

                keys.distinct() shouldBe keys
            }
        }

        @Test
        fun `every stand-in is a loading card, which the card renders inert`() {
            // ----- Act & Assert -----
            LibraryGridLayout.entries.forEach { layout ->
                layout.standInCards().forEach { card ->
                    card.isLoading shouldBe true
                }
            }
        }

        @Test
        fun `every layout gets stand-ins of the variant that layout draws`() {
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
                layout.standInCards().forEach { card ->
                    card.variant::class shouldBe expected.getValue(layout)
                }
            }
        }

        @Test
        fun `list layouts get rows of the matching size`() {
            // ----- Act & Assert -----
            LibraryGridLayout.LIST_COMPACT.standInCards().forEach { card ->
                card.variant.shouldBeInstanceOf<BookCardVariant.Row>().size shouldBe BookCardVariant.Row.Size.Compact
            }
            LibraryGridLayout.LIST_LARGE.standInCards().forEach { card ->
                card.variant.shouldBeInstanceOf<BookCardVariant.Row>().size shouldBe BookCardVariant.Row.Size.Large
            }
        }
    }
}
