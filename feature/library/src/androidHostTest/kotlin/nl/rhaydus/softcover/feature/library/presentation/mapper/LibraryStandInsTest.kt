package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
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
        fun `every covered stand-in card's cover is loading and has no source`() {
            // ----- Act & Assert -----
            LibraryGridLayout.entries.filterNot { it == LibraryGridLayout.LIST_COMPACT }.forEach { layout ->
                layout.standInCards().forEach { card ->
                    val cover = card.standInCover().shouldNotBeNull()

                    cover.isLoading shouldBe true
                    cover.source.shouldBeNull()
                }
            }
        }

        @Test
        fun `the compact list stand-ins are coverless rows`() {
            // ----- Act & Assert -----
            LibraryGridLayout.LIST_COMPACT.standInCards().forEach { card ->
                card.standInCover().shouldBeNull()
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

    private fun BookCardUiModel.standInCover(): CoverUiModel? =
        when (val variant = variant) {
            is BookCardVariant.Column -> variant.cover?.model

            is BookCardVariant.CoverOnly -> variant.cover.model

            is BookCardVariant.Row -> variant.lead?.shouldBeInstanceOf<BookCardLead.Cover>()?.cover?.model
        }
}
