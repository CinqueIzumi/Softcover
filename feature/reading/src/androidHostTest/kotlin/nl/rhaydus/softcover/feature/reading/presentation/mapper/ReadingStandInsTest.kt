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
            // ----- Arrange -----
            val cards = ALSO_READING_STAND_IN_CARDS

            // ----- Act -----
            val covers = cards.map { it.standInCover() }

            // ----- Assert -----
            cards.shouldNotBeEmpty()
            covers.forEach { cover ->
                cover.isLoading shouldBe true
                cover.source.shouldBeNull()
            }
        }

        @Test
        fun `every pick-up-next stand-in's cover is loading and has no source`() {
            // ----- Arrange -----
            val cards = PICK_UP_NEXT_STAND_IN_CARDS

            // ----- Act -----
            val covers = cards.map { it.standInCover() }

            // ----- Assert -----
            cards.shouldNotBeEmpty()
            covers.forEach { cover ->
                cover.isLoading shouldBe true
                cover.source.shouldBeNull()
            }
        }

        @Test
        fun `each stand-in list has distinct keys`() {
            // ----- Arrange -----
            val alsoReadingKeys = ALSO_READING_STAND_IN_CARDS.map { it.key }
            val pickUpNextKeys = PICK_UP_NEXT_STAND_IN_CARDS.map { it.key }

            // ----- Act -----
            val distinctAlsoReading = alsoReadingKeys.distinct()
            val distinctPickUpNext = pickUpNextKeys.distinct()

            // ----- Assert -----
            distinctAlsoReading shouldBe alsoReadingKeys
            distinctPickUpNext shouldBe pickUpNextKeys
        }
    }
}
