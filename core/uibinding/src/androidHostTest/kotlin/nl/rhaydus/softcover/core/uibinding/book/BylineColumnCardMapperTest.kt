package nl.rhaydus.softcover.core.uibinding.book

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BylineColumnCardMapperTest {
    private val cover = mockk<CoverUiModel>()

    private val overlay = CoverOverlayUiModel(
        badge = BadgeUiModel(
            label = "Out soon",
            tone = BadgeTone.Primary,
        ),
        corner = CoverOverlayCorner.TopStart,
        grayscale = false,
    )

    private fun book(
        id: Int = 7,
        authors: List<Author> = emptyList(),
    ): Book = Book(
        id = id,
        title = "Piranesi",
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = authors,
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
    )

    private fun BookCardUiModel.column(): BookCardVariant.Column =
        variant.shouldBeInstanceOf<BookCardVariant.Column>()

    @Nested
    inner class ToBylineColumnCard {
        @Test
        fun `prefixes the author string with By as the byline`() {
            // ----- Arrange -----
            val input = book(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Susanna Clarke",
                    ),
                    Author(
                        id = 2,
                        name = "Neil Gaiman",
                    ),
                ),
            )

            // ----- Act -----
            val card = input.toBylineColumnCard(
                cover = cover,
                coverBadge = overlay,
            )

            // ----- Assert -----
            card.column().text.byline shouldBe "By Susanna Clarke, Neil Gaiman"
            card.column().text.title shouldBe "Piranesi"
            card.column().cover shouldBe BookCardCover(
                model = cover,
                badge = overlay,
            )
            card.key shouldBe BookCardKey.Book(bookId = 7)
        }

        @Test
        fun `omits the byline when there is no author`() {
            // ----- Arrange -----
            val input = book(authors = emptyList())

            // ----- Act -----
            val card = input.toBylineColumnCard(
                cover = null,
                coverBadge = null,
            )

            // ----- Assert -----
            card.column().text.byline.shouldBeNull()
        }

        @Test
        fun `has no cover when none is given`() {
            // ----- Arrange -----
            val input = book()

            // ----- Act -----
            val card = input.toBylineColumnCard(
                cover = null,
                coverBadge = overlay,
            )

            // ----- Assert -----
            card.column().cover.shouldBeNull()
        }

        @Test
        fun `offers the overflow menu only when the rail asks for it`() {
            // ----- Arrange -----
            val input = book()

            // ----- Act -----
            val withMenu = input.toBylineColumnCard(
                cover = cover,
                coverBadge = null,
                hasMenu = true,
            )
            val withoutMenu = input.toBylineColumnCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            withMenu.column().hasMenu shouldBe true
            withoutMenu.column().hasMenu shouldBe false
        }

        @Test
        fun `threads the surface to the title transition key`() {
            // ----- Arrange -----
            val input = book(id = 9)

            // ----- Act -----
            val card = input.toBylineColumnCard(
                cover = cover,
                coverBadge = null,
                transitionSurface = "becauseYouRead",
            )

            // ----- Assert -----
            card.column().text.titleTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Title,
                bookId = 9,
                surface = "becauseYouRead",
            )
        }
    }

    @Nested
    inner class BylineColumnStandIns {
        @Test
        fun `produces the requested count of loading cards under distinct keys`() {
            // ----- Arrange -----
            val count = 4

            // ----- Act -----
            val cards = bylineColumnStandIns(count = count)

            // ----- Assert -----
            cards.size shouldBe count
            cards.map { it.key }.distinct().size shouldBe count
            cards.all { it.isLoading } shouldBe true
        }

        @Test
        fun `gives every stand-in the overflow menu only when asked, so it does not pop in later`() {
            // ----- Arrange -----
            val count = 3

            // ----- Act -----
            val withMenu = bylineColumnStandIns(
                count = count,
                hasMenu = true,
            )
            val withoutMenu = bylineColumnStandIns(count = count)

            // ----- Assert -----
            withMenu.all { it.column().hasMenu } shouldBe true
            withoutMenu.none { it.column().hasMenu } shouldBe true
        }

        @Test
        fun `produces no cards for a zero count`() {
            // ----- Arrange -----
            val count = 0

            // ----- Act -----
            val cards = bylineColumnStandIns(count = count)

            // ----- Assert -----
            cards.size shouldBe 0
        }
    }
}
