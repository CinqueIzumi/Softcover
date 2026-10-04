package nl.rhaydus.softcover.feature.reading.presentation.mapper

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingStandInsTest {
    private fun BookCardUiModel.standInCover(): CoverUiModel =
        when (val variant = variant) {
            is BookCardVariant.Column -> variant.cover.shouldNotBeNull().model

            is BookCardVariant.CoverOnly -> variant.cover.model

            is BookCardVariant.Row -> variant.lead.shouldBeInstanceOf<BookCardLead.Cover>().cover.model
        }

    @Nested
    inner class StandInCards {
        @Test
        fun `every also-reading stand-in's cover is loading and has no source`() {
            // ----- Act & Assert -----
            ALSO_READING_STAND_IN_CARDS.shouldNotBeEmpty()
            ALSO_READING_STAND_IN_CARDS.forEach { card ->
                val cover = card.standInCover()

                cover.isLoading shouldBe true
                cover.source.shouldBeNull()
            }
        }

        @Test
        fun `every pick-up-next stand-in's cover is loading and has no source`() {
            // ----- Act & Assert -----
            PICK_UP_NEXT_STAND_IN_CARDS.shouldNotBeEmpty()
            PICK_UP_NEXT_STAND_IN_CARDS.forEach { card ->
                val cover = card.standInCover()

                cover.isLoading shouldBe true
                cover.source.shouldBeNull()
            }
        }

        @Test
        fun `each stand-in list has distinct keys`() {
            // ----- Act & Assert -----
            ALSO_READING_STAND_IN_CARDS.map { it.key }.distinct() shouldBe ALSO_READING_STAND_IN_CARDS.map { it.key }
            PICK_UP_NEXT_STAND_IN_CARDS.map { it.key }.distinct() shouldBe PICK_UP_NEXT_STAND_IN_CARDS.map { it.key }
        }
    }
}
