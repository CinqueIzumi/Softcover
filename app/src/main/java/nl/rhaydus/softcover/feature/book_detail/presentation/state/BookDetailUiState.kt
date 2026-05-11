package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.book_detail.domain.model.BookReview
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.deadlines.domain.model.DeadlineProgress
import nl.rhaydus.softcover.feature.reading.presentation.enums.ProgressSheetTab
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle

data class BookDetailUiState(
    val loadingBookDetails: Boolean = true,
    val book: Book? = null,
    val fabMenuExpanded: Boolean = false,
    val showEditEditionSheet: Boolean = false,
    val editions: List<BookEdition> = emptyList(),
    val loadingEditions: Boolean = false,
    val editionSearchQuery: String = "",
    val showUpdateProgressSheet: Boolean = false,
    val selectedProgressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,

    val settingEditionOwned: Boolean = false,

    val deadline: BookDeadline? = null,
    val deadlineProgress: DeadlineProgress? = null,
    val showDeadlinePicker: Boolean = false,

    val reviews: List<BookReview> = emptyList(),
    val loadingReviews: Boolean = false,
    val revealedSpoilerReviewIds: Set<Int> = emptySet(),

    val failedMutationBookIds: Set<Int> = emptySet(),
    val failedMutationEditionIds: Set<Int> = emptySet(),
) : UiState {
    val filteredEditions: List<BookEdition>
        get() {
            val query = editionSearchQuery.trim()

            if (query.isEmpty()) return editions

            return editions.filter { edition ->
                edition.isbn10?.contains(other = query, ignoreCase = true) == true ||
                    edition.publisher?.contains(other = query, ignoreCase = true) == true
            }
        }
}
