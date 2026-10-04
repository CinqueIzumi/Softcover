package nl.rhaydus.softcover.feature.library.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
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
    ): Book = mockk<Book>(relaxed = true).also { book ->
        every {
            book.id
        } returns 1

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
        fun `grid progress carries the stored progress as the bar fraction`() {
            // ----- Arrange -----
            val book = stubBook(
                status = BookStatus.Reading,
                progress = 42f,
            )

            // ----- Act -----
            val progress = book.card(LibraryGridLayout.GRID_TWO_COLUMNS).decorations.progress

            // ----- Assert -----
            progress.shouldBeInstanceOf<BookCardProgress.Value>().fraction shouldBe 0.42f
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
}
