package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.badge.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DeadlineProgress
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

    /**
     * Every book's raw deadline progress, mapped off the composition by `DeadlineModelsCollector`
     * (R9) from [books] and [deadlines]. Stays a domain type rather than a UI model because
     * `planTodayNudgeFor` still reads it directly.
     */
    val deadlineProgressByBook: Map<Int, DeadlineProgress> = emptyMap(),

    /** Each book's deadline cover overlay badge, derived from [deadlineProgressByBook]. */
    val deadlineCoverOverlays: Map<Int, CoverOverlayUiModel> = emptyMap(),

    /**
     * Each book's deadline summary line for surfaces painted [nl.rhaydus.softcover.core.component
     * .badge.DeadlineSummaryTone.OnSurface], derived from [deadlineProgressByBook].
     */
    val deadlineSummaries: Map<Int, DeadlineSummaryUiModel> = emptyMap(),

    /**
     * The featured book's ([books] `.firstOrNull()`) deadline summary line, in the
     * [nl.rhaydus.softcover.core.component.badge.DeadlineSummaryTone.OnHeroBackdrop] ink for the
     * blurred-cover hero backdrop — the same book as [deadlineSummaries] would key on, but a
     * different UI model, because that surface takes a different ink. Kept as its own field rather
     * than a second map for the same reason [featuredCover] / [featuredBackdropCover] /
     * [featuredBookPace] are their own fields rather than keyed off [books].
     */
    val featuredDeadlineSummary: DeadlineSummaryUiModel? = null,
) : UiState
