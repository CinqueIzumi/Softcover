package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.BookTransitionKey
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExploreBookCardMapperTest {
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
        title: String = "Piranesi",
        rating: Double = 0.0,
        authors: List<Author> = emptyList(),
        bookSeries: BookSeries? = null,
        positionsInSeries: List<Double> = emptyList(),
        userBook: UserBook? = null,
        releaseYear: Int = 2020,
        usersCount: Int = 0,
    ): Book = Book(
        id = id,
        title = title,
        editions = emptyList(),
        defaultEdition = null,
        rating = rating,
        description = "",
        releaseYear = releaseYear,
        coverUrl = "",
        authors = authors,
        usersCount = usersCount,
        ratingsCount = 0,
        bookSeries = bookSeries,
        positionsInSeries = positionsInSeries,
        isCompilation = false,
        userBook = userBook,
    )

    private fun titleKey(
        bookId: Int,
        surface: String? = null,
    ): BookTransitionKey = bookTransitionKey(
        element = BookTransitionElement.Title,
        bookId = bookId,
        surface = surface,
    )

    private fun ratingKey(
        bookId: Int,
        surface: String? = null,
    ): BookTransitionKey = bookTransitionKey(
        element = BookTransitionElement.Rating,
        bookId = bookId,
        surface = surface,
    )

    private fun BookCardUiModel.stacked(): BookCardVariant.Column =
        variant.shouldBeInstanceOf<BookCardVariant.Column>()

    private fun BookCardUiModel.row(): BookCardVariant.Row =
        variant.shouldBeInstanceOf<BookCardVariant.Row>()

    @Nested
    inner class ToTrendingCard {
        @Test
        fun `keys the card by book id and passes the cover, title and badge through`() {
            // ----- Arrange -----
            val input = book(
                id = 7,
                title = "Piranesi",
            )

            // ----- Act -----
            val card = input.toTrendingCard(
                cover = cover,
                coverBadge = overlay,
            )

            // ----- Assert -----
            card.key shouldBe BookCardKey.Book(bookId = 7)
            card.stacked().cover shouldBe BookCardCover(
                model = cover,
                badge = overlay,
            )
            card.stacked().text.title shouldBe "Piranesi"
        }

        @Test
        fun `renders a one-decimal rating line for a rated book`() {
            // ----- Arrange -----
            val input = book(rating = 4.46)

            // ----- Act -----
            val card = input.toTrendingCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.supportingLines shouldBe persistentListOf(
                persistentListOf(
                    BookCardLinePart.Rating(
                        label = "4.5",
                        contentDescription = "Rated 4.5 out of 5",
                        transitionKey = ratingKey(bookId = 7),
                    ),
                ),
            )
        }

        @Test
        fun `omits the rating line when the rating is zero`() {
            // ----- Arrange -----
            val input = book(rating = 0.0)

            // ----- Act -----
            val card = input.toTrendingCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.supportingLines shouldBe persistentListOf()
        }
    }

    @Nested
    inner class ToUpNextCard {
        private val series = BookSeries(
            id = 3,
            name = "Earthsea",
            amountOfBooks = 6,
        )

        @Test
        fun `uses the series name as eyebrow and a Book number line`() {
            // ----- Arrange -----
            val input = book(
                bookSeries = series,
                positionsInSeries = listOf(2.0),
            )

            // ----- Act -----
            val card = input.toUpNextCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.eyebrow.shouldNotBeNull().text shouldBe "EARTHSEA"
            card.stacked().text.supportingLines shouldBe persistentListOf(
                persistentListOf(BookCardLinePart.Text(text = "Book #2")),
            )
        }

        @Test
        fun `omits the Book number line when there is no position`() {
            // ----- Arrange -----
            val input = book(
                bookSeries = series,
                positionsInSeries = emptyList(),
            )

            // ----- Act -----
            val card = input.toUpNextCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.eyebrow.shouldNotBeNull().text shouldBe "EARTHSEA"
            card.stacked().text.supportingLines shouldBe persistentListOf()
        }

        @Test
        fun `omits the eyebrow when the book has no series`() {
            // ----- Arrange -----
            val input = book(bookSeries = null)

            // ----- Act -----
            val card = input.toUpNextCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.eyebrow.shouldBeNull()
        }
    }

    @Nested
    inner class ToSearchResultCard {
        @Test
        fun `builds a row keyed by book id with the cover as lead`() {
            // ----- Arrange -----
            val input = book(id = 42)

            // ----- Act -----
            val card = input.toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.key shouldBe BookCardKey.Book(bookId = 42)
            card.row().lead shouldBe BookCardLead.Cover(cover = BookCardCover(model = cover))
        }

        @Test
        fun `offers to add a book that is not in the library`() {
            // ----- Arrange -----
            val input = book(userBook = null)

            // ----- Act -----
            val card = input.toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.row().trailing shouldBe BookCardTrailing.Bookmark.labelled(
                saved = false,
                contentDescription = "Add to library",
            )
        }

        @Test
        fun `offers to remove a book that is in the library`() {
            // ----- Arrange -----
            val savedBook = UserBook(
                id = 1,
                status = BookStatus.WantToRead,
                dateAdded = "2024-01-01",
                createdAt = null,
                privacySettingId = 1,
                reviewHasSpoilers = false,
                editionId = null,
                lastReadDate = null,
                rating = null,
                referrerUserId = null,
                reviewedAt = null,
                updatedAt = null,
                journals = emptyList(),
                reads = emptyList(),
            )
            val input = book(userBook = savedBook)

            // ----- Act -----
            val card = input.toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.row().trailing shouldBe BookCardTrailing.Bookmark.labelled(
                saved = true,
                contentDescription = "Remove from library",
            )
        }

        @Test
        fun `uses the series text as eyebrow only when the book is in a series`() {
            // ----- Arrange -----
            val inSeriesInput = book(
                bookSeries = BookSeries(
                    id = 1,
                    name = "Earthsea",
                    amountOfBooks = 3,
                ),
                positionsInSeries = listOf(2.0),
            )
            val standaloneInput = book(bookSeries = null)

            // ----- Act -----
            val inSeries = inSeriesInput.toSearchResultCard(cover = cover)
            val standalone = standaloneInput.toSearchResultCard(cover = cover)

            // ----- Assert -----
            inSeries.row().text.eyebrow.shouldNotBeNull().text shouldBe "#2 OF 3 IN EARTHSEA"
            standalone.row().text.eyebrow.shouldBeNull()
        }

        @Test
        fun `prefixes the byline with By and drops it when the author is blank`() {
            // ----- Arrange -----
            val withAuthorInput = book(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Ann",
                    ),
                ),
            )
            val withoutAuthorInput = book(authors = emptyList())

            // ----- Act -----
            val withAuthor = withAuthorInput.toSearchResultCard(cover = cover)
            val withoutAuthor = withoutAuthorInput.toSearchResultCard(cover = cover)

            // ----- Assert -----
            withAuthor.row().text.byline shouldBe "By Ann"
            withoutAuthor.row().text.byline.shouldBeNull()
        }

        @Test
        fun `shows the stats line with year readers and rating`() {
            // ----- Arrange -----
            val input = book(
                releaseYear = 2021,
                usersCount = 12,
                rating = 4.5,
            )

            // ----- Act -----
            val card = input.toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.row().text.supportingLines shouldBe persistentListOf(
                persistentListOf(
                    BookCardLinePart.Text(text = "2021"),
                    BookCardLinePart.Text(text = "12 readers"),
                    BookCardLinePart.Rating(
                        label = "4.5",
                        contentDescription = "Rated 4.5 out of 5",
                        transitionKey = ratingKey(bookId = 7),
                    ),
                ),
            )
        }

        @Test
        fun `leaves the year and rating out of the stats line when unknown`() {
            // ----- Arrange -----
            val input = book(
                releaseYear = -1,
                usersCount = 5,
                rating = 0.0,
            )

            // ----- Act -----
            val card = input.toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.row().text.supportingLines shouldBe persistentListOf(
                persistentListOf(BookCardLinePart.Text(text = "5 readers")),
            )
        }
    }

    @Nested
    inner class TransitionKeys {
        @Test
        fun `trending threads the surface to the title and rating keys`() {
            // ----- Arrange -----
            val input = book(
                id = 7,
                rating = 4.5,
            )

            // ----- Act -----
            val card = input.toTrendingCard(
                cover = cover,
                coverBadge = null,
                transitionSurface = "trending",
            )

            // ----- Assert -----
            val text = card.stacked().text
            text.titleTransitionKey shouldBe titleKey(
                bookId = 7,
                surface = "trending",
            )
            val rating = text.supportingLines.single().single().shouldBeInstanceOf<BookCardLinePart.Rating>()
            rating.transitionKey shouldBe ratingKey(
                bookId = 7,
                surface = "trending",
            )
        }

        @Test
        fun `one book on two surfaces gets distinct title and rating keys`() {
            // ----- Arrange -----
            val input = book(rating = 4.5)

            // ----- Act -----
            val first = input.toTrendingCard(
                cover = cover,
                coverBadge = null,
                transitionSurface = "trending",
            ).stacked().text
            val second = input.toTrendingCard(
                cover = cover,
                coverBadge = null,
                transitionSurface = "upNext",
            ).stacked().text

            // ----- Assert -----
            first.titleTransitionKey.shouldNotBeNull() shouldNotBe second.titleTransitionKey
            first.supportingLines shouldNotBe second.supportingLines
        }

        @Test
        fun `trending omits the rating key along with the rating line for an unrated book`() {
            // ----- Arrange -----
            val input = book(rating = 0.0)

            // ----- Act -----
            val text = input.toTrendingCard(
                cover = cover,
                coverBadge = null,
                transitionSurface = "trending",
            ).stacked().text

            // ----- Assert -----
            text.supportingLines shouldBe persistentListOf()
            text.titleTransitionKey shouldBe titleKey(
                bookId = 7,
                surface = "trending",
            )
        }

        @Test
        fun `up next threads the surface to the title key`() {
            // ----- Arrange -----
            val input = book(id = 9)

            // ----- Act -----
            val text = input.toUpNextCard(
                cover = cover,
                coverBadge = null,
                transitionSurface = "upNext",
            ).stacked().text

            // ----- Assert -----
            text.titleTransitionKey shouldBe titleKey(
                bookId = 9,
                surface = "upNext",
            )
        }

        @Test
        fun `a search result threads the surface to the title and rating keys`() {
            // ----- Arrange -----
            val input = book(
                id = 3,
                rating = 4.5,
            )

            // ----- Act -----
            val text = input.toSearchResultCard(
                cover = cover,
                transitionSurface = "search",
            ).row().text

            // ----- Assert -----
            text.titleTransitionKey shouldBe titleKey(
                bookId = 3,
                surface = "search",
            )
            val ratings = text.supportingLines.flatten().filterIsInstance<BookCardLinePart.Rating>()
            ratings.map { it.transitionKey } shouldBe listOf(
                ratingKey(
                    bookId = 3,
                    surface = "search",
                ),
            )
        }

        @Test
        fun `without a surface the keys carry no surface suffix`() {
            // ----- Arrange -----
            val input = book(id = 3)

            // ----- Act -----
            val text = input.toSearchResultCard(cover = cover).row().text

            // ----- Assert -----
            text.titleTransitionKey shouldBe titleKey(bookId = 3)
        }
    }
}
