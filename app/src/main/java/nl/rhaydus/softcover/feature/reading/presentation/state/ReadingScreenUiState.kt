package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.deadlines.domain.model.BookDeadline
import nl.rhaydus.softcover.feature.reading.presentation.enums.ProgressSheetTab
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle

data class ReadingScreenUiState(
    val books: List<Book>? = null,
    val isLoading: Boolean = false,
    val bookToUpdate: Book? = null,
    val progressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,
    val showProgressSheet: Boolean = false,

    val deadlines: Map<Int, BookDeadline> = emptyMap(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,

    val failedMutationBookIds: Set<Int> = emptySet(),
    val dismissedPlanTodayByBook: Map<Int, String> = emptyMap(),

    val wantToReadBooks: List<Book> = emptyList(),
    val trendingBooks: List<Book> = emptyList(),
) : UiState
