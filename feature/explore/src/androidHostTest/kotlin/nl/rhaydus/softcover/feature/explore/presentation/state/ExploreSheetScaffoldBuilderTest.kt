package nl.rhaydus.softcover.feature.explore.presentation.state

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookSeries
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExploreSheetScaffoldBuilderTest {
    private fun stubBook(
        title: String,
        bookSeries: BookSeries?,
    ): Book = mockk {
        every {
            this@mockk.title
        } returns title

        every {
            this@mockk.bookSeries
        } returns bookSeries
    }

    @Nested
    inner class ContinueSeriesMenuSheetScaffoldFor {
        @Test
        fun `uses the series name as eyebrow and the book title as headline`() {
            // ----- Arrange -----
            val book = stubBook(
                title = "The Final Empire",
                bookSeries = BookSeries(
                    id = 1,
                    name = "Mistborn Era",
                    amountOfBooks = 3,
                ),
            )

            // ----- Act -----
            val result = continueSeriesMenuSheetScaffoldFor(book)

            // ----- Assert -----
            result.header.section shouldBe SectionHeaderUiModel.Section(
                eyebrow = "Mistborn Era",
                headline = "The Final Empire",
            )
        }

        @Test
        fun `falls back to Up next as eyebrow when the book has no series`() {
            // ----- Arrange -----
            val book = stubBook(
                title = "Elantris",
                bookSeries = null,
            )

            // ----- Act -----
            val result = continueSeriesMenuSheetScaffoldFor(book)

            // ----- Assert -----
            result.header.section shouldBe SectionHeaderUiModel.Section(
                eyebrow = "Up next",
                headline = "Elantris",
            )
        }
    }
}
