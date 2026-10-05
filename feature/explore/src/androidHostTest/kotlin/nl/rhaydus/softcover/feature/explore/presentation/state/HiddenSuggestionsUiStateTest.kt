package nl.rhaydus.softcover.feature.explore.presentation.state

import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardLead
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.feature.explore.presentation.action.HiddenSuggestionsAction
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HiddenSuggestionsUiStateTest {
    private fun realCard(id: Int): BookCardUiModel = BookCardUiModel(
        key = BookCardKey.Book(bookId = id),
        variant = BookCardVariant.Row(
            size = BookCardVariant.Row.Size.Compact,
            lead = BookCardLead.SeriesStack(front = mockk<CoverUiModel>()),
            text = BookCardContent(title = "Book $id"),
        ),
    )

    private fun setOf(vararg ids: Int): BookCardSet<HiddenSuggestionsAction> =
        BookCardSet.standIns(ids.map(::realCard).toImmutableList())

    // ----- isLoadingBooks / isLoadingSeries -----

    @Nested
    inner class IsLoading {
        @Test
        fun `reports both lists loading before any data lands`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState()

            // ----- Act -----
            val books = state.isLoadingBooks
            val series = state.isLoadingSeries

            // ----- Assert -----
            books shouldBe true
            series shouldBe true
        }

        @Test
        fun `reports books not loading once real book cards replace the stand-ins`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(hiddenBooks = setOf(1))

            // ----- Act -----
            val result = state.isLoadingBooks

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `reports books not loading when the book cards are empty`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(hiddenBooks = BookCardSet.empty())

            // ----- Act -----
            val result = state.isLoadingBooks

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `reports series not loading once real series cards replace the stand-ins`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(hiddenSeries = setOf(1))

            // ----- Act -----
            val result = state.isLoadingSeries

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `keeps series loading while only the books have landed`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(hiddenBooks = BookCardSet.empty())

            // ----- Act -----
            val result = state.isLoadingSeries

            // ----- Assert -----
            result shouldBe true
        }
    }

    // ----- isEmpty -----

    @Nested
    inner class IsEmpty {
        @Test
        fun `is false while both card lists are still stand-ins`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState()

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false while only the books have landed and are empty`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(hiddenBooks = BookCardSet.empty())

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is true when both card lists have landed empty and nothing is hidden`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(
                hiddenBooks = BookCardSet.empty(),
                hiddenSeries = BookCardSet.empty(),
            )

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe true
        }

        @Test
        fun `is false when both have landed but a book is hidden`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(
                hiddenBooks = setOf(1),
                hiddenSeries = BookCardSet.empty(),
            )

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }

        @Test
        fun `is false when both have landed but a series is hidden`() {
            // ----- Arrange -----
            val state = HiddenSuggestionsUiState(
                hiddenBooks = BookCardSet.empty(),
                hiddenSeries = setOf(2),
            )

            // ----- Act -----
            val result = state.isEmpty

            // ----- Assert -----
            result shouldBe false
        }
    }
}
