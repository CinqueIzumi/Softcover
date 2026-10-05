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
            // ----- Arrange -----
            val keysPerLayout = LibraryGridLayout.entries.map { layout -> layout.standInCards().map { it.key } }

            // ----- Act -----
            val distinctKeysPerLayout = keysPerLayout.map { it.distinct() }

            // ----- Assert -----
            distinctKeysPerLayout shouldBe keysPerLayout
        }

        @Test
        fun `every stand-in is a loading card whose cover, where it has one, is loading with no source`() {
            // ----- Arrange -----
            val layouts = LibraryGridLayout.entries.filterNot { it == LibraryGridLayout.LIST_COMPACT }

            // ----- Act -----
            val cardsPerLayout = layouts.map { it.standInCards() }

            // ----- Assert -----
            cardsPerLayout.flatten().forEach { card ->
                card.isLoading shouldBe true
                val cover = card.standInCover().shouldNotBeNull()

                cover.isLoading shouldBe true
                cover.source.shouldBeNull()
            }
        }

        @Test
        fun `the compact list stand-ins are loading coverless rows`() {
            // ----- Act -----
            val cards = LibraryGridLayout.LIST_COMPACT.standInCards()

            // ----- Assert -----
            cards.forEach { card ->
                card.isLoading shouldBe true
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

            // ----- Act -----
            val cardsPerLayout = LibraryGridLayout.entries.associateWith { it.standInCards() }

            // ----- Assert -----
            cardsPerLayout.forEach { (layout, cards) ->
                cards.forEach { card ->
                    card.variant::class shouldBe expected.getValue(layout)
                }
            }
        }

        @Test
        fun `list layouts get rows of the matching size`() {
            // ----- Act -----
            val compactCards = LibraryGridLayout.LIST_COMPACT.standInCards()
            val largeCards = LibraryGridLayout.LIST_LARGE.standInCards()

            // ----- Assert -----
            compactCards.forEach { card ->
                card.variant.shouldBeInstanceOf<BookCardVariant.Row>().size shouldBe BookCardVariant.Row.Size.Compact
            }
            largeCards.forEach { card ->
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
