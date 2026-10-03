package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardCover
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.bookcard.BookCardSelection
import nl.rhaydus.softcover.core.component.bookcard.BookCardTrailing
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EditionBookCardMapperTest {
    private fun BookEdition.card(
        selected: Boolean = false,
        tracked: Boolean = false,
        fallbackEdition: BookEdition? = null,
    ): BookCardUiModel = toEditionCard(
        fallbackEdition = fallbackEdition,
        selected = selected,
        tracked = tracked,
    )

    private fun BookCardUiModel.row(): BookCardVariant.Row = variant.shouldBeInstanceOf<BookCardVariant.Row>()

    private fun BookCardUiModel.coverPairing(): BookCardCover =
        row().lead.shouldBeInstanceOf<BookCardLead.Cover>().cover

    private fun BookCardUiModel.lines(): List<String> = row().text.supportingLines.map { line ->
        line.parts.single().shouldBeInstanceOf<BookCardLinePart.Text>().text
    }

    private fun BookCardUiModel.linePartTexts(): List<List<String>> = row().text.supportingLines.map { line ->
        line.parts.map { it.shouldBeInstanceOf<BookCardLinePart.Text>().text }
    }

    @Nested
    inner class ToEditionCard {
        @Test
        fun `keys the card by edition id with a standard row that taps`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 42)

            // ----- Act -----
            val card = edition.card()

            // ----- Assert -----
            card.key shouldBe BookCardKey.Edition(editionId = 42)
            card.row().size shouldBe BookCardVariant.Row.Size.Standard
            card.decorations.pressBehaviour shouldBe BookCardDecorations.Press.Tap
        }

        @Test
        fun `titles the card with the format label`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                format = "Hardcover",
                readingFormat = ReadingFormat.Audio,
            )

            // ----- Act -----
            val card = edition.card()

            // ----- Assert -----
            card.row().text.title shouldBe "Hardcover"
        }

        @Test
        fun `falls back to the reading format label when the format is blank`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                format = "  ",
                readingFormat = ReadingFormat.Ebook,
            )

            // ----- Act -----
            val card = edition.card()

            // ----- Assert -----
            card.row().text.title shouldBe "E-book"
        }

        @Test
        fun `falls back to Edition when the format is blank and there is no reading format`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                format = "",
                readingFormat = null,
            )

            // ----- Act -----
            val card = edition.card()

            // ----- Assert -----
            card.row().text.title shouldBe "Edition"
        }

        @Test
        fun `picks the format glyph per reading format`() {
            // ----- Arrange -----
            val expected = mapOf(
                ReadingFormat.Audio to SoftcoverIcon.Headset,
                ReadingFormat.Ebook to SoftcoverIcon.LibraryBooks,
                ReadingFormat.Physical to SoftcoverIcon.MenuBook,
                ReadingFormat.Both to SoftcoverIcon.MenuBook,
            )

            // ----- Act -----
            val actual = expected.keys.associateWith {
                buildEdition(id = 1).copy(readingFormat = it).card().row().text.titleIcon
            }

            // ----- Assert -----
            actual shouldBe expected
            buildEdition(id = 1).copy(readingFormat = null).card().row().text.titleIcon shouldBe SoftcoverIcon.MenuBook
        }

        @Test
        fun `splits publisher year and pages into separate parts of one meta line`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                publisher = "Tor",
                releaseYear = 2019,
                pages = 320,
            )

            // ----- Act -----
            val lines = edition.card().linePartTexts()

            // ----- Assert -----
            lines shouldBe listOf(listOf("Tor", "2019", "320 pp"))
        }

        @Test
        fun `drops unknown parts from the meta line`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                publisher = " ",
                releaseYear = -1,
                pages = 100,
            )

            // ----- Act -----
            val lines = edition.card().lines()

            // ----- Assert -----
            lines shouldBe listOf("100 pp")
        }

        @Test
        fun `shows the audio duration instead of pages for an audiobook`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                publisher = "Audible",
                releaseYear = 2021,
                pages = 300,
                audioSeconds = 3 * 3600 + 25 * 60 + 10,
                readingFormat = ReadingFormat.Audio,
            )

            // ----- Act -----
            val lines = edition.card().linePartTexts()

            // ----- Assert -----
            lines shouldBe listOf(listOf("Audible", "2021", "3h 25m"))
        }

        @Test
        fun `omits the meta line when every part is unknown`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                publisher = null,
                releaseYear = -1,
                pages = null,
            )

            // ----- Act -----
            val lines = edition.card().lines()

            // ----- Assert -----
            lines.shouldBeEmpty()
        }

        @Test
        fun `adds an ISBN line after the meta line`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                publisher = "Tor",
                releaseYear = -1,
                isbn13 = "9781234567897",
            )

            // ----- Act -----
            val lines = edition.card().lines()

            // ----- Assert -----
            lines shouldBe listOf("Tor", "ISBN 9781234567897")
        }

        @Test
        fun `prefers ISBN-13 over ISBN-10`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                releaseYear = -1,
                isbn10 = "1234567890",
                isbn13 = "9781234567897",
            )

            // ----- Act -----
            val lines = edition.card().lines()

            // ----- Assert -----
            lines shouldBe listOf("ISBN 9781234567897")
        }

        @Test
        fun `falls back to ISBN-10 when ISBN-13 is blank`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                releaseYear = -1,
                isbn10 = "1234567890",
                isbn13 = "",
            )

            // ----- Act -----
            val lines = edition.card().lines()

            // ----- Assert -----
            lines shouldBe listOf("ISBN 1234567890")
        }

        @Test
        fun `marks every row as a single selection and selects only the picked one`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1)

            // ----- Act -----
            val selected = edition.card(selected = true)
            val unselected = edition.card(selected = false)

            // ----- Assert -----
            selected.decorations.selection shouldBe BookCardSelection.Single(selected = true)
            unselected.decorations.selection shouldBe BookCardSelection.Single(selected = false)
        }

        @Test
        fun `badges the cover of an owned edition only`() {
            // ----- Arrange -----
            val owned = buildEdition(id = 1).copy(owned = true)
            val notOwned = buildEdition(id = 2).copy(owned = false)

            // ----- Act -----
            val ownedCard = owned.card()
            val notOwnedCard = notOwned.card()

            // ----- Assert -----
            ownedCard.coverPairing().badge.shouldNotBeNull()
            notOwnedCard.coverPairing().badge.shouldBeNull()
        }

        @Test
        fun `trails a badge on the tracked edition only`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1)

            // ----- Act -----
            val tracked = edition.card(tracked = true)
            val untracked = edition.card(tracked = false)

            // ----- Assert -----
            tracked.row().trailing.shouldBeInstanceOf<BookCardTrailing.Badge>()
            untracked.row().trailing.shouldBeNull()
        }
    }
}
