package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
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

    /**
     * What the progress sheet renders, mapped off the composition by `ProgressSheetCollector`
     * (R9) from [bookToUpdate] and [progressSheetTab] — never by the render.
     */
    val progressSheet: ProgressSheetUiModel? = null,

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
    val verdictReview: RichTextUiModel? = null,

    /**
     * Every cover surface on this screen, mapped off the composition by `CoverModelsCollector`
     * (R9) from [books], [wantToReadBooks], [trendingBooks] and [verdictPromptBook] — never by the
     * render.
     */
    val featuredBackdropCover: CoverUiModel? = null,
    val featuredCover: CoverUiModel? = null,
    val bookCovers: Map<Int, CoverUiModel> = emptyMap(),

    /**
     * Mirrors [nl.rhaydus.softcover.feature.reading.presentation.screen.EmptyCurrentlyReadingScreen]'s
     * own `wantToReadBooks.take(3)` — kept in step here because that render decision is duplicated,
     * not derived from this map, so a change to the empty screen's tile count must update both.
     */
    val pickUpNextCovers: Map<Int, CoverUiModel> = emptyMap(),
    val trendingTileCover: CoverUiModel? = null,
    val verdictCover: CoverUiModel? = null,
) : UiState
