package nl.rhaydus.softcover.feature.reading.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.cover.CoverSource
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TrendingTileMapperTest {
    private fun book(
        title: String = "Piranesi",
        authors: List<Author> = emptyList(),
        coverUrl: String = "https://cover",
    ): Book = Book(
        id = 7,
        title = title,
        editions = emptyList(),
        defaultEdition = null,
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = coverUrl,
        authors = authors,
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = null,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
        userBookRead = null,
    )

    @Nested
    inner class ToTrendingTileUiModel {
        @Test
        fun `uses the book title`() {
            // ----- Arrange -----
            val book = book(title = "Piranesi")

            // ----- Act -----
            val result = book.toTrendingTileUiModel()

            // ----- Assert -----
            result.title shouldBe "Piranesi"
        }

        @Test
        fun `shows only the first author`() {
            // ----- Arrange -----
            val book = book(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "First Author",
                    ),
                    Author(
                        id = 2,
                        name = "Second Author",
                    ),
                ),
            )

            // ----- Act -----
            val result = book.toTrendingTileUiModel()

            // ----- Assert -----
            result.author shouldBe "First Author"
        }

        @Test
        fun `leaves the author null when the book has none`() {
            // ----- Arrange -----
            val book = book(authors = emptyList())

            // ----- Act -----
            val result = book.toTrendingTileUiModel()

            // ----- Assert -----
            result.author.shouldBeNull()
        }

        @Test
        fun `falls back to the book cover url for a book without editions`() {
            // ----- Arrange -----
            val book = book(coverUrl = "https://cover/piranesi")

            // ----- Act -----
            val result = book.toTrendingTileUiModel()

            // ----- Assert -----
            val cover = result.cover.shouldNotBeNull()
            cover.source shouldBe CoverSource.Remote(
                url = "https://cover/piranesi",
                persistEditionId = null,
            )
        }

        @Test
        fun `builds a tile cover titled with the book that shares its transition`() {
            // ----- Arrange -----
            val book = book(title = "Piranesi")

            // ----- Act -----
            val result = book.toTrendingTileUiModel()

            // ----- Assert -----
            val cover = result.cover.shouldNotBeNull()
            cover.variant shouldBe CoverVariant.Tile
            cover.coverlessTitle shouldBe "Piranesi"
            cover.sharedTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Cover,
                bookId = 7,
                surface = null,
            )
        }
    }
}
