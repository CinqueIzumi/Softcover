package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.richtext.RichTextUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.personal.domain.model.ReadingPaceForecast
import nl.rhaydus.softcover.feature.reading.presentation.collector.mastheadEyebrowFor
import nl.rhaydus.softcover.feature.reading.presentation.collector.mastheadRefreshFor
import nl.rhaydus.toad.UiState

internal data class ReadingScreenUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true,

    /**
     * The "also reading" section opener, mapped off the composition by
     * `CurrentlyReadingBooksCollector` (R9) from [books]`.drop(1)`'s size — never by the render.
     */
    val alsoReadingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "0 more in motion",
        headline = "Also between your fingers",
    ),
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

    val pickUpNextHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "Pick up next"),
    val trendingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "Trending now"),

    val recentReadingActivity: List<ReadingDayActivity> = emptyList(),
    val streakEnabled: Boolean = true,

    val streakStripHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Last 0 days",
        headline = "No reading logged yet",
    ),

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
     * Mirrors [nl.rhaydus.softcover.feature.reading.presentation.screen.section.EmptyCurrentlyReadingScreen]'s
     * own `wantToReadBooks.take(3)` — kept in step here because that render decision is duplicated,
     * not derived from this map, so a change to the empty screen's tile count must update both.
     */
    val pickUpNextCovers: Map<Int, CoverUiModel> = emptyMap(),
    val trendingTileCover: CoverUiModel? = null,
    val verdictCover: CoverUiModel? = null,

    /**
     * Each book's deadline cover overlay badge, mapped off the composition by
     * `DeadlineModelsCollector` (R9) — never by the render.
     */
    val deadlineCoverOverlays: Map<Int, CoverOverlayUiModel> = emptyMap(),

    /**
     * Each book's deadline summary line for surfaces painted [nl.rhaydus.softcover.core.component
     * .deadline.DeadlineSummaryTone.OnSurface], mapped off the composition by `DeadlineModelsCollector`
     * (R9) — never by the render.
     */
    val deadlineSummaries: Map<Int, DeadlineSummaryUiModel> = emptyMap(),

    /**
     * The featured book's ([books] `.firstOrNull()`) deadline summary line, in the
     * [nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryTone.OnHeroBackdrop] ink for the
     * blurred-cover hero backdrop — the same book as [deadlineSummaries] would key on, but a
     * different UI model, because that surface takes a different ink. Kept as its own field rather
     * than a second map for the same reason [featuredCover] / [featuredBackdropCover] /
     * [featuredBookPace] are their own fields rather than keyed off [books].
     */
    val featuredDeadlineSummary: DeadlineSummaryUiModel? = null,

    /**
     * The featured book's plan-today nudge copy, mapped off the composition by
     * `DeadlineModelsCollector` (R9). `null` means either there is nothing to say today or the
     * reader already dismissed it — [dismissedPlanTodayByBook] is folded in upstream.
     */
    val featuredPlanTodayMessage: String? = null,

    /**
     * The desktop masthead's eyebrow, greeting title and subtitle, mapped off the composition by
     * `MastheadCollector` (R9/R10) from [books] and [isLoading] — never by the render.
     */
    val masthead: PageMastheadUiModel = PageMastheadUiModel(
        eyebrow = mastheadEyebrowFor(isLoading = true),
        title = "Good morning.",
        subtitle = "0 titles in motion",
    ),

    /** The same greeting and subtitle at [PageMastheadSize.Compact], for the mobile header. */
    val compactMasthead: PageMastheadUiModel = PageMastheadUiModel(
        title = "Good morning.",
        subtitle = "0 titles in motion",
        size = PageMastheadSize.Compact,
    ),

    /** [masthead]'s trailing refresh action label and enabled state, tracking [isLoading]. */
    val mastheadRefresh: MastheadRefreshUiModel = mastheadRefreshFor(isLoading = true),
) : UiState
