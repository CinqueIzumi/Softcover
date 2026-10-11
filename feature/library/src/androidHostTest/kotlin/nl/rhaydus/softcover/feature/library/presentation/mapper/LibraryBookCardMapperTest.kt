package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.designsystem.presentation.transition.BookTransitionElement
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.UserBookRead
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class LibraryBookCardMapperTest {
    private fun stubBook(
        authorString: String = "",
        seriesText: String? = null,
        status: BookStatus = BookStatus.Read,
        progress: Float? = null,
        rating: Double = 0.0,
    ): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns 1

        every {
            book.currentEdition
        } returns stubEdition(authorString = "")

        every {
            book.rating
        } returns rating

        every {
            book.title
        } returns "Title"

        every {
            book.authorString
        } returns authorString

        every {
            book.seriesText
        } returns seriesText

        every {
            book.status
        } returns status

        every {
            book.userBookRead
        } returns progress?.let { value ->
            mockk<UserBookRead>(relaxed = true).also { read ->
                every {
                    read.progress
                } returns value
            }
        }
    }

    private fun stubEdition(authorString: String): BookEdition = mockk<BookEdition>(relaxed = true).also { edition ->
        every {
            edition.id
        } returns 7

        every {
            edition.title
        } returns "Edition"

        every {
            edition.authorString
        } returns authorString
    }

    private fun Book.card(layout: LibraryGridLayout) = toBookCard(
        layout = layout,
        press = BookCardDecorations.Press.Tap,
        selection = null,
        deadline = null,
        dateStyle = DateStyle.entries.first(),
    )

    private fun BookEdition.card(layout: LibraryGridLayout) = toEditionCard(
        layout = layout,
        press = BookCardDecorations.Press.Tap,
    )

    private fun BookCardVariant.byline(): String? = when (this) {
        is BookCardVariant.Column -> text.byline
        is BookCardVariant.Row -> text.byline
        is BookCardVariant.CoverOnly -> null
    }

    @Nested
    inner class BookByline {
        @Test
        fun `is the author string prefixed with By on every text variant`() {
            // ----- Arrange -----
            val book = stubBook(authorString = "Ann, Bob")
            val layouts = listOf(
                LibraryGridLayout.GRID_TWO_COLUMNS,
                LibraryGridLayout.GRID_THREE_COLUMNS,
                LibraryGridLayout.LIST_COMPACT,
                LibraryGridLayout.LIST_LARGE,
            )

            // ----- Act -----
            val bylines = layouts.map { book.card(it).variant.byline() }

            // ----- Assert -----
            bylines shouldBe layouts.map { "By Ann, Bob" }
        }

        @Test
        fun `is null for a blank author string`() {
            // ----- Arrange -----
            val book = stubBook(authorString = " ")

            // ----- Act -----
            val byline = book.card(LibraryGridLayout.LIST_LARGE).variant.byline()

            // ----- Assert -----
            byline.shouldBeNull()
        }

        @Test
        fun `cover-only layouts carry no text`() {
            // ----- Arrange -----
            val book = stubBook(authorString = "Ann")

            // ----- Act -----
            val variant = book.card(LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY).variant

            // ----- Assert -----
            variant.shouldBeInstanceOf<BookCardVariant.CoverOnly>()
        }
    }

    @Nested
    inner class EditionByline {
        @Test
        fun `is the edition author string prefixed with By on every text variant`() {
            // ----- Arrange -----
            val edition = stubEdition(authorString = "Ann, Bob")
            val layouts = listOf(
                LibraryGridLayout.GRID_TWO_COLUMNS,
                LibraryGridLayout.LIST_COMPACT,
                LibraryGridLayout.LIST_LARGE,
            )

            // ----- Act -----
            val bylines = layouts.map { edition.card(it).variant.byline() }

            // ----- Assert -----
            bylines shouldBe layouts.map { "By Ann, Bob" }
        }

        @Test
        fun `is null for a blank author string`() {
            // ----- Arrange -----
            val edition = stubEdition(authorString = "")

            // ----- Act -----
            val byline = edition.card(LibraryGridLayout.LIST_COMPACT).variant.byline()

            // ----- Assert -----
            byline.shouldBeNull()
        }
    }

    @Nested
    inner class LargeRowExtras {
        private fun Book.largeRow() = card(LibraryGridLayout.LIST_LARGE).variant
            .shouldBeInstanceOf<BookCardVariant.Row>()

        @Test
        fun `the series eyebrow is uppercase`() {
            // ----- Arrange -----
            val book = stubBook(seriesText = "Dune Chronicles")

            // ----- Act -----
            val eyebrow = book.largeRow().text.eyebrow

            // ----- Assert -----
            eyebrow.shouldNotBeNull().text shouldBe "DUNE CHRONICLES"
        }

        @Test
        fun `a blank series has no eyebrow`() {
            // ----- Arrange -----
            val book = stubBook(seriesText = " ")

            // ----- Act -----
            val eyebrow = book.largeRow().text.eyebrow

            // ----- Assert -----
            eyebrow.shouldBeNull()
        }

        @Test
        fun `a reading book carries a percent progress label`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Reading,
                progress = 42f,
            )

            // ----- Act -----
            val progress = book.card(LibraryGridLayout.LIST_LARGE).decorations.progress

            // ----- Assert -----
            val value = progress.shouldBeInstanceOf<BookCardProgress.Value>()
            value.label shouldBe "42%"
            value.fraction shouldBe 0.42f
        }

        @Test
        fun `a book that is not being read has no progress on the large row`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Read,
                progress = 42f,
            )

            // ----- Act -----
            val progress = book.card(LibraryGridLayout.LIST_LARGE).decorations.progress

            // ----- Assert -----
            progress.shouldBeNull()
        }

        @Test
        fun `grid progress carries no label`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Reading,
                progress = 42f,
            )

            // ----- Act -----
            val progress = book.card(LibraryGridLayout.GRID_TWO_COLUMNS).decorations.progress

            // ----- Assert -----
            progress.shouldBeInstanceOf<BookCardProgress.Value>().label.shouldBeNull()
        }

        @Test
        fun `the large row passes out-of-range progress through raw while the label is capped`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Reading,
                progress = 150f,
            )

            // ----- Act -----
            val progress = book.card(LibraryGridLayout.LIST_LARGE).decorations.progress

            // ----- Assert -----
            val value = progress.shouldBeInstanceOf<BookCardProgress.Value>()
            value.fraction shouldBe 1.5f
            value.label shouldBe "100%"
        }

        @Test
        fun `grid progress passes out-of-range progress through raw`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Reading,
                progress = -5f,
            )

            // ----- Act -----
            val progress = book.card(LibraryGridLayout.GRID_TWO_COLUMNS).decorations.progress

            // ----- Assert -----
            progress.shouldBeInstanceOf<BookCardProgress.Value>().fraction shouldBe -0.05f
        }
    }

    @Nested
    inner class TransitionKeys {
        private fun BookCardVariant.titleKey(): String? = when (this) {
            is BookCardVariant.Column -> text.titleTransitionKey
            is BookCardVariant.Row -> text.titleTransitionKey
            is BookCardVariant.CoverOnly -> null
        }

        private fun BookCardVariant.ratingKeys(): List<String?> = (this as? BookCardVariant.Row)
            ?.text
            ?.supportingLines
            .orEmpty()
            .flatten()
            .filterIsInstance<BookCardLinePart.Rating>()
            .map { it.transitionKey }

        @Test
        fun `the column title key is built from the same ids as its cover key`() {
            // ----- Arrange -----
            val book = stubBook()

            // ----- Act -----
            val variant = book.card(LibraryGridLayout.GRID_TWO_COLUMNS).variant

            // ----- Assert -----
            val column = variant.shouldBeInstanceOf<BookCardVariant.Column>()
            val expectedTitle = bookTransitionKey(
                element = BookTransitionElement.Title,
                editionId = 7,
                bookId = 1,
            )
            val expectedCover = bookTransitionKey(
                element = BookTransitionElement.Cover,
                editionId = 7,
                bookId = 1,
            )
            column.text.titleTransitionKey.shouldNotBeNull() shouldBe expectedTitle
            column.cover.shouldNotBeNull().model.sharedTransitionKey shouldBe expectedCover
        }

        @Test
        fun `the large row title key is built from the same ids as its cover key`() {
            // ----- Arrange -----
            val book = stubBook()

            // ----- Act -----
            val variant = book.card(LibraryGridLayout.LIST_LARGE).variant

            // ----- Assert -----
            val row = variant.shouldBeInstanceOf<BookCardVariant.Row>()
            val lead = row.lead.shouldBeInstanceOf<BookCardLead.Cover>()
            row.text.titleTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Title,
                editionId = 7,
                bookId = 1,
            )
            lead.cover.model.sharedTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Cover,
                editionId = 7,
                bookId = 1,
            )
        }

        @Test
        fun `the compact row has no cover but still carries the title key`() {
            // ----- Arrange -----
            val book = stubBook()

            // ----- Act -----
            val variant = book.card(LibraryGridLayout.LIST_COMPACT).variant

            // ----- Assert -----
            val row = variant.shouldBeInstanceOf<BookCardVariant.Row>()
            row.lead.shouldBeNull()
            row.text.titleTransitionKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Title,
                editionId = 7,
                bookId = 1,
            )
        }

        @Test
        fun `the large row rating part carries the rating key`() {
            // ----- Arrange -----
            val book = stubBook(rating = 4.5)

            // ----- Act -----
            val keys = book.card(LibraryGridLayout.LIST_LARGE).variant.ratingKeys()

            // ----- Assert -----
            keys shouldBe listOf(
                bookTransitionKey(
                    element = BookTransitionElement.Rating,
                    editionId = 7,
                    bookId = 1,
                ),
            )
        }

        @Test
        fun `the large row has no rating part to key when the book is unrated`() {
            // ----- Arrange -----
            val book = stubBook(rating = 0.0)

            // ----- Act -----
            val keys = book.card(LibraryGridLayout.LIST_LARGE).variant.ratingKeys()

            // ----- Assert -----
            keys shouldHaveSize 0
        }

        @Test
        fun `the title key falls back to the book id when there is no current edition`() {
            // ----- Arrange -----
            val book = stubBook()
            every {
                book.currentEdition
            } returns null

            // ----- Act -----
            val titleKey = book.card(LibraryGridLayout.GRID_TWO_COLUMNS).variant.titleKey()

            // ----- Assert -----
            titleKey shouldBe bookTransitionKey(
                element = BookTransitionElement.Title,
                editionId = null,
                bookId = 1,
            )
        }

        @Test
        fun `cover-only cards carry no title key`() {
            // ----- Arrange -----
            val book = stubBook()

            // ----- Act -----
            val titleKey = book.card(LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY).variant.titleKey()

            // ----- Assert -----
            titleKey.shouldBeNull()
        }

        @Test
        fun `edition cards carry no title or rating key on any layout`() {
            // ----- Arrange -----
            val edition = stubEdition(authorString = "Ann")

            // ----- Act -----
            val variants = LibraryGridLayout.entries.map { edition.card(it).variant }

            // ----- Assert -----
            variants.map { it.titleKey() }.all { it == null } shouldBe true
            variants.flatMap { it.ratingKeys() }.shouldBeEmpty()
        }
    }
}
