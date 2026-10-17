package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExploreStandInsTest {
    private fun BookCardUiModel.standInCover(): CoverUiModel =
        when (val variant = variant) {
            is BookCardVariant.Column -> variant.cover.shouldNotBeNull().model

            is BookCardVariant.CoverOnly -> variant.cover.model

            is BookCardVariant.Row -> when (val lead = variant.lead) {
                is BookCardLead.Cover -> lead.cover.model

                is BookCardLead.SeriesStack -> lead.front

                else -> error("Unexpected stand-in lead $lead")
            }
        }

    private val standInLists: Map<String, ImmutableList<BookCardUiModel>> = mapOf(
        "trending" to TRENDING_STAND_IN_CARDS,
        "because you read" to BECAUSE_YOU_READ_STAND_IN_CARDS,
        "up next" to UP_NEXT_STAND_IN_CARDS,
        "search result" to SEARCH_RESULT_STAND_IN_CARDS,
        "hidden book" to HIDDEN_BOOK_STAND_IN_CARDS,
        "hidden series" to HIDDEN_SERIES_STAND_IN_CARDS,
    )

    @Nested
    inner class StandInCards {
        @Test
        fun `every stand-in card's cover is loading and has no source`() {
            // ----- Arrange -----
            val lists = standInLists.values

            // ----- Act & Assert -----
            lists.forEach { cards ->
                cards.shouldNotBeEmpty()
                cards.forEach { card ->
                    val cover = card.standInCover()

                    cover.isLoading shouldBe true
                    cover.source.shouldBeNull()
                }
            }
        }

        @Test
        fun `each stand-in list has distinct keys`() {
            // ----- Arrange -----
            val lists = standInLists.values

            // ----- Act & Assert -----
            lists.forEach { cards ->
                val keys = cards.map { it.key }

                keys.distinct() shouldBe keys
            }
        }
    }
}
