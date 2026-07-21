package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.core.designsystem.presentation.model.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.toad.UiState

internal data class ReadingScreenUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val bookToUpdate: Book? = null,
    val progressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,
    val showProgressSheet: Boolean = false,

    val deadlines: Map<Int, BookDeadline> = emptyMap(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,

    val failedMutationBookIds: Set<Int> = emptySet(),
    val dismissedPlanTodayByBook: Map<Int, String> = emptyMap(),

    val wantToReadBooks: List<Book> = emptyList(),
    val trendingBooks: List<Book> = emptyList(),

    val recentReadingActivity: List<ReadingDayActivity> = emptyList(),
    val streakEnabled: Boolean = true,

    val featuredBookPace: ReadingPaceForecast? = null,

    val verdictPromptBook: Book? = null,
) : UiState
