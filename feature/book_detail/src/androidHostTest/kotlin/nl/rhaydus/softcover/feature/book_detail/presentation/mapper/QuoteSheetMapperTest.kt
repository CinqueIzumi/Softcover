package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nl.rhaydus.softcover.core.component.quote.QuotePositionTab
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import nl.rhaydus.softcover.feature.book_detail.presentation.buildQuoteBook
import nl.rhaydus.softcover.feature.book_detail.presentation.state.QuoteDraft
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class QuoteSheetMapperTest {
    private fun draft(
        tab: QuotePositionTab = QuotePositionTab.PAGE,
        text: String = "A line worth keeping",
        positionText: String = "",
        hours: String = "",
        minutes: String = "",
        seconds: String = "",
        progressAdvanceEnabled: Boolean = true,
    ): QuoteDraft = QuoteDraft(
        tab = tab,
        privacy = PrivacySetting.PUBLIC,
        text = text,
        positionText = positionText,
        hours = hours,
        minutes = minutes,
        seconds = seconds,
        progressAdvanceEnabled = progressAdvanceEnabled,
    )

    @Nested
    inner class PositionTabs {
        @Test
        fun `offers page then percentage for a paged edition with a page total`() {
            // ----- Arrange -----
            val book = buildQuoteBook(currentPages = 437)

            // ----- Act -----
            val tabs = book.quotePositionTabs

            // ----- Assert -----
            tabs shouldBe listOf(QuotePositionTab.PAGE, QuotePositionTab.PERCENTAGE)
        }

        @Test
        fun `falls back to the default edition pages when the current edition has none`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                currentPages = null,
                defaultPages = 300,
            )

            // ----- Act -----
            val tabs = book.quotePositionTabs

            // ----- Assert -----
            tabs shouldBe listOf(QuotePositionTab.PAGE, QuotePositionTab.PERCENTAGE)
        }

        @Test
        fun `offers percentage alone when no edition has a page total`() {
            // ----- Arrange -----
            val book = buildQuoteBook()

            // ----- Act -----
            val tabs = book.quotePositionTabs

            // ----- Assert -----
            tabs shouldBe listOf(QuotePositionTab.PERCENTAGE)
        }

        @Test
        fun `offers time then percentage for an audiobook with an audio total`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                isAudiobook = true,
                audioSeconds = 36_000,
                currentPages = 300,
            )

            // ----- Act -----
            val tabs = book.quotePositionTabs

            // ----- Assert -----
            tabs shouldBe listOf(QuotePositionTab.TIME, QuotePositionTab.PERCENTAGE)
        }

        @Test
        fun `offers percentage alone for an audiobook without an audio total`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                isAudiobook = true,
                audioSeconds = null,
                currentPages = 300,
            )

            // ----- Act -----
            val tabs = book.quotePositionTabs

            // ----- Assert -----
            tabs shouldBe listOf(QuotePositionTab.PERCENTAGE)
        }

        @Test
        fun `shows the primary tab when the draft's tab is no longer offered`() {
            // ----- Arrange -----
            val book = buildQuoteBook(currentPages = 437)

            // ----- Act -----
            val model = book.toQuoteSheetUiModel(draft = draft(tab = QuotePositionTab.TIME))

            // ----- Assert -----
            model.positionTabs.selected shouldBe QuotePositionTab.PAGE
        }
    }

    @Nested
    inner class ToPosition {
        @Test
        fun `is null while the position text is blank`() {
            // ----- Arrange -----
            val book = buildQuoteBook(currentPages = 437)

            // ----- Act -----
            val position = draft(positionText = "").toPosition(book = book)

            // ----- Assert -----
            position.shouldBeNull()
        }

        @Test
        fun `measures a page against the page total`() {
            // ----- Arrange -----
            val book = buildQuoteBook(currentPages = 437)

            // ----- Act -----
            val position = draft(positionText = "214").toPosition(book = book)

            // ----- Assert -----
            position shouldBe QuotePosition.pages(
                page = 214,
                totalPages = 437,
            )
        }

        @Test
        fun `is null for page zero`() {
            // ----- Arrange -----
            val book = buildQuoteBook(currentPages = 437)

            // ----- Act -----
            val position = draft(positionText = "0").toPosition(book = book)

            // ----- Assert -----
            position.shouldBeNull()
        }

        @Test
        fun `reads the percentage tab as a percent`() {
            // ----- Arrange -----
            val book = buildQuoteBook(currentPages = 437)

            // ----- Act -----
            val position = draft(
                tab = QuotePositionTab.PERCENTAGE,
                positionText = "53",
            ).toPosition(book = book)

            // ----- Assert -----
            position shouldBe QuotePosition.Percent(percent = 53.0)
        }

        @Test
        fun `adds hours minutes and seconds into one time position`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                isAudiobook = true,
                audioSeconds = 36_000,
            )

            // ----- Act -----
            val position = draft(
                tab = QuotePositionTab.TIME,
                hours = "1",
                minutes = "2",
                seconds = "3",
            ).toPosition(book = book)

            // ----- Assert -----
            position shouldBe QuotePosition.seconds(
                seconds = 3723,
                totalSeconds = 36_000,
            )
        }

        @Test
        fun `is null when every time field is blank`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                isAudiobook = true,
                audioSeconds = 36_000,
            )

            // ----- Act -----
            val position = draft(tab = QuotePositionTab.TIME).toPosition(book = book)

            // ----- Assert -----
            position.shouldBeNull()
        }
    }

    @Nested
    inner class SanitizedQuoteNumber {
        @Test
        fun `keeps digits only`() {
            // ----- Arrange -----
            val text = "1a2-b3"

            // ----- Act -----
            val result = sanitizedQuoteNumber(
                text = text,
                maximum = 999,
                maxLength = 4,
            )

            // ----- Assert -----
            result shouldBe "123"
        }

        @Test
        fun `caps the number at the maximum`() {
            // ----- Arrange -----
            val text = "900"

            // ----- Act -----
            val result = sanitizedQuoteNumber(
                text = text,
                maximum = 437,
                maxLength = 4,
            )

            // ----- Assert -----
            result shouldBe "437"
        }

        @Test
        fun `cuts the text to the max length before capping`() {
            // ----- Arrange -----
            val text = "12345"

            // ----- Act -----
            val result = sanitizedQuoteNumber(
                text = text,
                maximum = 9999,
                maxLength = 3,
            )

            // ----- Assert -----
            result shouldBe "123"
        }

        @Test
        fun `stays blank when there are no digits so the field can be cleared`() {
            // ----- Arrange -----
            val text = "abc"

            // ----- Act -----
            val result = sanitizedQuoteNumber(
                text = text,
                maximum = 100,
                maxLength = 3,
            )

            // ----- Assert -----
            result shouldBe ""
        }
    }

    @Nested
    inner class ProgressAdvanceRow {
        @Test
        fun `offers to move a paged read forward to the quote's page`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                currentPages = 437,
                readPage = 180,
            )

            // ----- Act -----
            val row = book.toQuoteSheetUiModel(draft = draft(positionText = "214")).progressAdvance

            // ----- Assert -----
            row.shouldNotBeNull().title shouldBe "Update my progress to page 214"
            row.gloss shouldBe "You're on page 180."
            row.checked shouldBe true
        }

        @Test
        fun `offers to mark the book as read on its last page`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                currentPages = 437,
                readPage = 180,
            )

            // ----- Act -----
            val row = book.toQuoteSheetUiModel(draft = draft(positionText = "437")).progressAdvance

            // ----- Assert -----
            row.shouldNotBeNull().title shouldBe "Mark as read"
            row.gloss shouldBe "Page 437 is the last page."
        }

        @Test
        fun `offers to move an audiobook forward to the quote's time`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                isAudiobook = true,
                audioSeconds = 36_000,
                readSeconds = 2700,
            )

            // ----- Act -----
            val row = book.toQuoteSheetUiModel(
                draft = draft(
                    tab = QuotePositionTab.TIME,
                    hours = "1",
                    minutes = "2",
                    seconds = "3",
                ),
            ).progressAdvance

            // ----- Assert -----
            row.shouldNotBeNull().title shouldBe "Update my progress to 1:02:03"
            row.gloss shouldBe "You're at 0:45:00."
        }

        @Test
        fun `offers to mark an audiobook as read at its end`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                isAudiobook = true,
                audioSeconds = 3600,
                readSeconds = 100,
            )

            // ----- Act -----
            val row = book.toQuoteSheetUiModel(
                draft = draft(
                    tab = QuotePositionTab.TIME,
                    hours = "1",
                ),
            ).progressAdvance

            // ----- Assert -----
            row.shouldNotBeNull().title shouldBe "Mark as read"
            row.gloss shouldBe "That's the end of the audiobook."
        }

        @Test
        fun `is hidden when the draft has no position`() {
            // ----- Arrange -----
            val book = buildQuoteBook(
                currentPages = 437,
                readPage = 180,
            )

            // ----- Act -----
            val row = book.toQuoteSheetUiModel(draft = draft()).progressAdvance

            // ----- Assert -----
            row.shouldBeNull()
        }
    }

    @Nested
    inner class Scaffold {
        @Test
        fun `disables save while the quote text is blank`() {
            // ----- Arrange -----
            val book = buildQuoteBook(title = "Piranesi")

            // ----- Act -----
            val model = book.toQuoteSheetUiModel(draft = draft(text = "   "))

            // ----- Assert -----
            val footer = model.scaffold.footer.shouldBeInstanceOf<SheetFooterUiModel.Stacked>()
            footer.primary.shouldNotBeNull().enabled shouldBe false
        }

        @Test
        fun `enables save once there is quote text`() {
            // ----- Arrange -----
            val book = buildQuoteBook(title = "Piranesi")

            // ----- Act -----
            val model = book.toQuoteSheetUiModel(draft = draft(text = "A line"))

            // ----- Assert -----
            val footer = model.scaffold.footer.shouldBeInstanceOf<SheetFooterUiModel.Stacked>()
            footer.primary.shouldNotBeNull().enabled shouldBe true
        }
    }
}
