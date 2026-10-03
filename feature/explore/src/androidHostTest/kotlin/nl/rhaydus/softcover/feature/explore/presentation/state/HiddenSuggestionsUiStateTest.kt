package nl.rhaydus.softcover.feature.explore.presentation.state

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HiddenSuggestionsUiStateTest {
    // ----- isEmpty -----

    @Nested
    inner class IsEmpty {
        @Test
        fun `is false when nothing has loaded`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState()

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when only the books flag is set and both lists are empty`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(hiddenBooksLoaded = true)

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when only the series flag is set and both lists are empty`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(hiddenSeriesLoaded = true)

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is true when both flags are set and both lists are empty`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(
                hiddenBooksLoaded = true,
                hiddenSeriesLoaded = true,
            )

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false when both are loaded but a book is hidden`() {
            // ----- Arrange -----
            val book = DismissedSeriesBook(
                bookId = 1,
                title = "Book",
                coverUrl = null,
                authorText = null,
                seriesName = null,
                seriesId = null,
                seriesPosition = null,
            )
            val state = HiddenSuggestionsUiState(
                hiddenBooks = listOf(book),
                hiddenBooksLoaded = true,
                hiddenSeriesLoaded = true,
            )

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when both are loaded but a series is hidden`() {
            // ----- Arrange -----
            val series = DismissedSeries(
                seriesId = 1,
                seriesName = "Series",
                coverUrl = null,
                authorText = null,
                bookCount = null,
            )
            val state = HiddenSuggestionsUiState(
                hiddenSeries = listOf(series),
                hiddenBooksLoaded = true,
                hiddenSeriesLoaded = true,
            )

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }
    }
}
