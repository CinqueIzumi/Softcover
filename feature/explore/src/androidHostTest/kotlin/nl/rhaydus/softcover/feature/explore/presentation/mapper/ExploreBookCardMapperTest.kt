package nl.rhaydus.softcover.feature.explore.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
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
        userBookRead = null,
    )

    private fun BookCardUiModel.stacked(): BookCardVariant.Column =
        variant.shouldBeInstanceOf<BookCardVariant.Column>()

    @Nested
    inner class ToTrendingCard {
        @Test
        fun `keys the card by book id and passes cover, title and badge through`() {
            // ----- Act -----
            val card = book(
                id = 7,
                title = "Piranesi",
            ).toTrendingCard(
                cover = cover,
                coverBadge = overlay,
            )

            // ----- Assert -----
            card.key shouldBe BookCardKey.Book(bookId = 7)
            card.stacked().cover shouldBe cover
            card.stacked().text.title shouldBe "Piranesi"
            card.decorations.coverBadge shouldBe overlay
            card.decorations.pressBehaviour shouldBe BookCardDecorations.Press.Tap
            card.decorations.progress.shouldBeNull()
        }

        @Test
        fun `renders a one-decimal rating line for a rated book`() {
            // ----- Act -----
            val card = book(rating = 4.46).toTrendingCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.supportingLines shouldBe persistentListOf(
                BookCardLine(
                    parts = persistentListOf(
                        BookCardLinePart.Rating(label = formatDecimalNumber(
                            4.46,
                            fractionDigits = 1,
                        ),),
                    ),
                ),
            )
        }

        @Test
        fun `omits the rating line when the rating is zero`() {
            // ----- Act -----
            val card = book(rating = 0.0).toTrendingCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.supportingLines shouldBe persistentListOf()
        }

        @Test
        fun `has no byline, eyebrow or overflow menu and tolerates a missing cover and badge`() {
            // ----- Act -----
            val card = book(rating = 3.0).toTrendingCard(
                cover = null,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().cover.shouldBeNull()
            card.stacked().text.byline.shouldBeNull()
            card.stacked().text.eyebrow.shouldBeNull()
            card.stacked().menuLabel.shouldBeNull()
            card.decorations.coverBadge.shouldBeNull()
        }
    }

    @Nested
    inner class ToBecauseYouReadCard {
        @Test
        fun `prefixes the author string with By as the byline`() {
            // ----- Act -----
            val card = book(
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
            ).toBecauseYouReadCard(
                cover = cover,
                coverBadge = overlay,
            )

            // ----- Assert -----
            card.stacked().text.byline shouldBe "By Susanna Clarke, Neil Gaiman"
            card.stacked().cover shouldBe cover
            card.decorations.coverBadge shouldBe overlay
            card.key shouldBe BookCardKey.Book(bookId = 7)
        }

        @Test
        fun `omits the byline when there is no author`() {
            // ----- Act -----
            val card = book(authors = emptyList()).toBecauseYouReadCard(
                cover = null,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.byline.shouldBeNull()
            card.stacked().text.supportingLines shouldBe persistentListOf()
            card.stacked().menuLabel.shouldBeNull()
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
            // ----- Act -----
            val card = book(
                bookSeries = series,
                positionsInSeries = listOf(2.0),
            ).toUpNextCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.eyebrow.shouldNotBeNull().text shouldBe "Earthsea"
            card.stacked().text.supportingLines shouldBe persistentListOf(
                BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = "Book #2"))),
            )
        }

        @Test
        fun `omits the Book number line when there is no position`() {
            // ----- Act -----
            val card = book(
                bookSeries = series,
                positionsInSeries = emptyList(),
            )
                .toUpNextCard(
                    cover = cover,
                    coverBadge = null,
                )

            // ----- Assert -----
            card.stacked().text.eyebrow.shouldNotBeNull().text shouldBe "Earthsea"
            card.stacked().text.supportingLines shouldBe persistentListOf()
        }

        @Test
        fun `omits the eyebrow when the book has no series`() {
            // ----- Act -----
            val card = book(bookSeries = null).toUpNextCard(
                cover = cover,
                coverBadge = null,
            )

            // ----- Assert -----
            card.stacked().text.eyebrow.shouldBeNull()
        }

        @Test
        fun `labels the overflow menu and tracks the overlay as cover badge with a tap and no progress`() {
            // ----- Act -----
            val card = book().toUpNextCard(
                cover = cover,
                coverBadge = overlay,
            )

            // ----- Assert -----
            card.stacked().menuLabel shouldBe "More options"
            card.decorations.coverBadge shouldBe overlay
            card.decorations.pressBehaviour shouldBe BookCardDecorations.Press.Tap
            card.decorations.progress.shouldBeNull()
        }
    }

    @Nested
    inner class ToSearchResultCard {
        private fun BookCardUiModel.row(): BookCardVariant.Row =
            variant.shouldBeInstanceOf<BookCardVariant.Row>()

        @Test
        fun `builds a standard row keyed by book id with the cover as lead`() {
            // ----- Act -----
            val card = book(id = 42).toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.key shouldBe BookCardKey.Book(bookId = 42)
            card.row().size shouldBe BookCardVariant.Row.Size.Standard
            card.row().lead shouldBe BookCardLead.Cover(model = cover)
        }

        @Test
        fun `offers to add a book that is not in the library`() {
            // ----- Act -----
            val card = book(userBook = null).toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.row().trailing shouldBe BookCardTrailing.Bookmark(
                saved = false,
                contentDescription = "Add to library",
            )
        }

        @Test
        fun `offers to remove a book that is in the library`() {
            // ----- Act -----
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
            )
            val card = book(userBook = savedBook).toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.row().trailing shouldBe BookCardTrailing.Bookmark(
                saved = true,
                contentDescription = "Remove from library",
            )
        }

        @Test
        fun `uses the series text as eyebrow only when the book is in a series`() {
            // ----- Act -----
            val inSeries = book(
                bookSeries = BookSeries(
                    id = 1,
                    name = "Earthsea",
                    amountOfBooks = 3,
                ),
                positionsInSeries = listOf(2.0),
            ).toSearchResultCard(cover = cover)
            val standalone = book(bookSeries = null).toSearchResultCard(cover = cover)

            // ----- Assert -----
            inSeries.row().text.eyebrow.shouldNotBeNull().text shouldBe "#2 of 3 in Earthsea"
            standalone.row().text.eyebrow.shouldBeNull()
        }

        @Test
        fun `prefixes the byline with By and drops it when the author is blank`() {
            // ----- Act -----
            val withAuthor = book(authors = listOf(Author(
                id = 1,
                name = "Ann",
            ),),)
                .toSearchResultCard(cover = cover)
            val withoutAuthor = book(authors = emptyList()).toSearchResultCard(cover = cover)

            // ----- Assert -----
            withAuthor.row().text.byline shouldBe "By Ann"
            withoutAuthor.row().text.byline.shouldBeNull()
        }

        @Test
        fun `shows the stats line with year readers and rating`() {
            // ----- Act -----
            val card = book(
                releaseYear = 2021,
                usersCount = 12,
                rating = 4.5,
            ).toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.row().text.supportingLines shouldBe persistentListOf(
                BookCardLine(
                    parts = persistentListOf(
                        BookCardLinePart.Text(text = "2021"),
                        BookCardLinePart.Text(text = "12 readers"),
                        BookCardLinePart.Rating(label = formatDecimalNumber(
                                4.5,
                                fractionDigits = 1,
                            ),),
                    ),
                ),
            )
        }

        @Test
        fun `leaves the year and rating out of the stats line when unknown`() {
            // ----- Act -----
            val card = book(
                releaseYear = -1,
                usersCount = 5,
                rating = 0.0,
            ).toSearchResultCard(cover = cover)

            // ----- Assert -----
            card.row().text.supportingLines shouldBe persistentListOf(
                BookCardLine(parts = persistentListOf(BookCardLinePart.Text(text = "5 readers"))),
            )
        }
    }
}
