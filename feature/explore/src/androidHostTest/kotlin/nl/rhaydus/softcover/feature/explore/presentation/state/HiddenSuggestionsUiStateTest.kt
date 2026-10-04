package nl.rhaydus.softcover.feature.explore.presentation.state

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
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
        fun `is false when only the book cards are loaded and both lists are empty`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(hiddenBookCards = persistentListOf())

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when only the series cards are loaded and both lists are empty`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(hiddenSeriesCards = persistentListOf())

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is true when both card lists are loaded and both lists are empty`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(
                hiddenBookCards = persistentListOf(),
                hiddenSeriesCards = persistentListOf(),
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
                hiddenBookCards = persistentListOf(),
                hiddenSeriesCards = persistentListOf(),
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
                hiddenBookCards = persistentListOf(),
                hiddenSeriesCards = persistentListOf(),
            )

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }
    }
}
