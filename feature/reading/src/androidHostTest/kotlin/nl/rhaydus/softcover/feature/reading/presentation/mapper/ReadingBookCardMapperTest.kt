package nl.rhaydus.softcover.feature.reading.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlin.time.Clock
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.component.badge.BadgeTone
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.BookTransitionKey
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookSeries
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
import nl.rhaydus.softcover.core.domain.model.DeadlineUnit
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ReadingBookCardMapperTest {
    private val overlay = CoverOverlayUiModel(
        badge = BadgeUiModel(
            label = "Behind",
            tone = BadgeTone.Primary,
        ),
        corner = CoverOverlayCorner.TopStart,
        grayscale = false,
    )

    private fun edition(authors: List<Author>): BookEdition = BookEdition(
        id = 50,
        canonicalId = null,
        bookId = 1,
        publisher = null,
        title = null,
        url = null,
        localImagePath = null,
        isbn10 = null,
        isbn13 = null,
        pages = 100,
        audioSeconds = null,
        authors = authors,
        releaseYear = 2020,
        format = "Paperback",
        owned = false,
    )

    private fun read(progress: Float): UserBookRead = UserBookRead(
        id = 1,
        currentPage = 0,
        currentSeconds = null,
        progress = progress,
        startedAt = null,
        finishedAt = null,
    )

    private fun book(
        id: Int = 1,
        title: String = "Title",
        series: BookSeries? = null,
        authors: List<Author>? = listOf(
            Author(
                id = 9,
                name = "Ann",
            ),
        ),
        userBookRead: UserBookRead? = null,
    ): Book = Book(
        id = id,
        title = title,
        editions = emptyList(),
        defaultEdition = authors?.let { edition(authors = it) },
        rating = 0.0,
        description = "",
        releaseYear = 2020,
        coverUrl = "",
        authors = emptyList(),
        usersCount = 0,
        ratingsCount = 0,
        bookSeries = series,
        positionsInSeries = emptyList(),
        isCompilation = false,
        userBook = null,
        userBookRead = userBookRead,
    )

    private fun alsoReading(
        book: Book,
        deadlineProgress: DeadlineProgress? = null,
        coverBadge: CoverOverlayUiModel? = null,
        mutationFailed: Boolean = false,
    ): BookCardUiModel = book.toAlsoReadingCard(
        deadlineProgress = deadlineProgress,
        coverBadge = coverBadge,
        dateStyle = DateStyle.DAY_MONTH_YEAR,
        mutationFailed = mutationFailed,
    )

    private fun BookCardUiModel.row(): BookCardVariant.Row =
        variant.shouldBeInstanceOf<BookCardVariant.Row>()

    private fun BookCardUiModel.coverPairing(): BookCardCover =
        row().lead.shouldBeInstanceOf<BookCardLead.Cover>().cover

    private fun progressValue(card: BookCardUiModel): BookCardProgress.Value =
        card.decorations.progress.shouldBeInstanceOf<BookCardProgress.Value>()

    @Nested
    inner class ToAlsoReadingCard {
        @Test
        fun `keys the card by book id and renders a standard row`() {
            // ----- Arrange -----
            val book = book(id = 7)

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            card.key shouldBe BookCardKey.Book(bookId = 7)
            card.row().size shouldBe BookCardVariant.Row.Size.Standard
            card.row().text.title shouldBe "Title"
            card.row().trailing.shouldBeInstanceOf<BookCardTrailing.SetProgress>()
        }

        @Test
        fun `shows the upper-cased series as the eyebrow`() {
            // ----- Arrange -----
            val book = book(
                series = BookSeries(
                    id = 4,
                    name = "Earthsea",
                    amountOfBooks = 3,
                ),
            )

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            card.row().text.eyebrow shouldBe BookCardEyebrow(text = "EARTHSEA")
        }

        @Test
        fun `has no eyebrow when the book has no series`() {
            // ----- Arrange -----
            val book = book(series = null)

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            card.row().text.eyebrow.shouldBeNull()
        }

        @Test
        fun `has no eyebrow when the series name is blank`() {
            // ----- Arrange -----
            val book = book(
                series = BookSeries(
                    id = 4,
                    name = "  ",
                    amountOfBooks = 3,
                ),
            )

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            card.row().text.eyebrow.shouldBeNull()
        }

        @Test
        fun `a failed save replaces the series eyebrow with an error eyebrow`() {
            // ----- Arrange -----
            val book = book(
                series = BookSeries(
                    id = 4,
                    name = "Earthsea",
                    amountOfBooks = 3,
                ),
            )

            // ----- Act -----
            val card = alsoReading(
                book = book,
                mutationFailed = true,
            )

            // ----- Assert -----
            val eyebrow = card.row().text.eyebrow.shouldNotBeNull()
            eyebrow.tone shouldBe BookCardEyebrow.Tone.Error
            eyebrow.text shouldNotBe "EARTHSEA"
        }

        @Test
        fun `a failed save shakes the row and a healthy one does not`() {
            // ----- Arrange -----
            val book = book()

            // ----- Act -----
            val failed = alsoReading(
                book = book,
                mutationFailed = true,
            )
            val healthy = alsoReading(book = book)

            // ----- Assert -----
            failed.decorations.shakeOnError shouldBe true
            healthy.decorations.shakeOnError shouldBe false
        }

        @Test
        fun `prefixes the edition authors with By`() {
            // ----- Arrange -----
            val book = book(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Ann",
                    ),
                    Author(
                        id = 2,
                        name = "Bob",
                    ),
                ),
            )

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            card.row().text.byline shouldBe "By Ann, Bob"
        }

        @Test
        fun `has no byline when the book has no edition`() {
            // ----- Arrange -----
            val book = book(authors = null)

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            card.row().text.byline.shouldBeNull()
        }

        @Test
        fun `has no byline when the author names are blank`() {
            // ----- Arrange -----
            val book = book(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "",
                    ),
                ),
            )

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            card.row().text.byline.shouldBeNull()
        }

        @Test
        fun `maps progress to a fraction and a rounded percentage label`() {
            // ----- Arrange -----
            val book = book(userBookRead = read(progress = 25f))

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            val progress = progressValue(card)
            progress.fraction shouldBe 0.25f
            progress.label shouldBe "25%"
        }

        @Test
        fun `rounds the percentage label to the nearest whole number`() {
            // ----- Arrange -----
            val book = book(userBookRead = read(progress = 42.6f))

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            progressValue(card).label shouldBe "43%"
        }

        @Test
        fun `shows zero percent when the book has no read`() {
            // ----- Arrange -----
            val book = book(userBookRead = null)

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            val progress = progressValue(card)
            progress.fraction shouldBe 0f
            progress.label shouldBe "0%"
        }

        @Test
        fun `caps the percentage label at one hundred when progress exceeds one hundred`() {
            // ----- Arrange -----
            val book = book(userBookRead = read(progress = 150f))

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            val progress = progressValue(card)
            progress.fraction shouldBe 1.5f
            progress.label shouldBe "100%"
        }

        @Test
        fun `passes the raw fraction through and floors the label when progress is negative`() {
            // ----- Arrange -----
            val book = book(userBookRead = read(progress = -5f))

            // ----- Act -----
            val card = alsoReading(book = book)

            // ----- Assert -----
            val progress = progressValue(card)
            progress.fraction shouldBe -0.05f
            progress.label shouldBe "0%"
        }

        @Test
        fun `carries the cover badge`() {
            // ----- Arrange -----
            val book = book()

            // ----- Act -----
            val withBadge = alsoReading(
                book = book,
                coverBadge = overlay,
            )
            val without = alsoReading(book = book)

            // ----- Assert -----
            withBadge.coverPairing().badge shouldBe overlay
            without.coverPairing().badge.shouldBeNull()
        }

        @Test
        fun `renders the deadline summary with the required pace`() {
            // ----- Arrange -----
            val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
            val deadlineProgress = DeadlineProgress.compute(
                deadline = BookDeadline(
                    bookId = 1,
                    deadlineDate = today.plus(DatePeriod(days = 10)),
                    setAt = today,
                    initialPerDay = 10f,
                    unit = DeadlineUnit.PAGES,
                ),
                current = 0,
                total = 100,
            )

            // ----- Act -----
            val card = alsoReading(
                book = book(),
                deadlineProgress = deadlineProgress,
            )

            // ----- Assert -----
            val summary = card.row().deadline.shouldNotBeNull()
            summary.text.substringAfter(" • ") shouldBe "10 pages/day"
        }
    }

    @Nested
    inner class ToPickUpNextCard {
        @Test
        fun `renders a column tile with the title only`() {
            // ----- Arrange -----
            val book = book(
                id = 3,
                title = "Piranesi",
                series = BookSeries(
                    id = 4,
                    name = "Earthsea",
                    amountOfBooks = 3,
                ),
                userBookRead = read(progress = 40f),
            )

            // ----- Act -----
            val card = book.toPickUpNextCard()

            // ----- Assert -----
            card.key shouldBe BookCardKey.Book(bookId = 3)
            val column = card.variant.shouldBeInstanceOf<BookCardVariant.Column>()
            column.text.title shouldBe "Piranesi"
            column.text.eyebrow.shouldBeNull()
            column.text.byline.shouldBeNull()
            column.hasMenu shouldBe false
            column.cover.shouldNotBeNull()
            card.decorations.progress.shouldBeNull()
        }
    }

    @Nested
    inner class TransitionKeys {
        private fun key(
            element: BookTransitionElement,
            bookId: Int,
        ): BookTransitionKey = bookTransitionKey(
            element = element,
            bookId = bookId,
        )

        @Test
        fun `the also reading title key matches its cover key's ids`() {
            // ----- Arrange -----
            val book = book(id = 7)

            // ----- Act -----
            val row = alsoReading(book = book).variant.shouldBeInstanceOf<BookCardVariant.Row>()

            // ----- Assert -----
            val lead = row.lead.shouldBeInstanceOf<BookCardLead.Cover>()
            row.text.titleTransitionKey shouldBe key(
                element = BookTransitionElement.Title,
                bookId = 7,
            )
            lead.cover.model.sharedTransitionKey shouldBe key(
                element = BookTransitionElement.Cover,
                bookId = 7,
            )
        }

        @Test
        fun `the also reading card carries no rating key because it shows no rating`() {
            // ----- Arrange -----
            val book = book()

            // ----- Act -----
            val row = alsoReading(book = book).variant.shouldBeInstanceOf<BookCardVariant.Row>()

            // ----- Assert -----
            row.text.supportingLines shouldBe persistentListOf()
        }

        @Test
        fun `the also reading title key falls back to the book id without an edition`() {
            // ----- Arrange -----
            val book = book(
                id = 7,
                authors = null,
            )

            // ----- Act -----
            val row = alsoReading(book = book).variant.shouldBeInstanceOf<BookCardVariant.Row>()

            // ----- Assert -----
            row.text.titleTransitionKey shouldBe key(
                element = BookTransitionElement.Title,
                bookId = 7,
            )
        }

        @Test
        fun `the pick up next title key matches its cover key's ids`() {
            // ----- Arrange -----
            val book = book(id = 3)

            // ----- Act -----
            val column = book.toPickUpNextCard().variant.shouldBeInstanceOf<BookCardVariant.Column>()

            // ----- Assert -----
            column.text.titleTransitionKey shouldBe key(
                element = BookTransitionElement.Title,
                bookId = 3,
            )
            column.cover.shouldNotBeNull().model.sharedTransitionKey shouldBe key(
                element = BookTransitionElement.Cover,
                bookId = 3,
            )
        }

        @Test
        fun `two different books get distinct title keys`() {
            // ----- Arrange -----
            val first = book(id = 1)
            val second = book(id = 2)

            // ----- Act -----
            val firstKey = first.toPickUpNextCard().variant
                .shouldBeInstanceOf<BookCardVariant.Column>().text.titleTransitionKey
            val secondKey = second.toPickUpNextCard().variant
                .shouldBeInstanceOf<BookCardVariant.Column>().text.titleTransitionKey

            // ----- Assert -----
            firstKey.shouldNotBeNull() shouldNotBe secondKey
        }
    }
}
