package nl.rhaydus.softcover.feature.reading.presentation.state

import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.callout.BannerUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryUiModel
import nl.rhaydus.softcover.core.component.header.MastheadRefreshUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadSize
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateButtonUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateVariant
import nl.rhaydus.softcover.core.component.verdict.VerdictSheetUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookDeadline
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.uibinding.masthead.mastheadRefreshFor
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.softcover.feature.reading.presentation.collector.mastheadEyebrowFor
import nl.rhaydus.softcover.feature.reading.presentation.mapper.ALSO_READING_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.reading.presentation.mapper.FEATURED_STAND_IN_BOOK
import nl.rhaydus.softcover.feature.reading.presentation.mapper.PICK_UP_NEXT_STAND_IN_CARDS
import nl.rhaydus.toad.UiState

internal data class ReadingScreenUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true,

    val alsoReadingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "0 more in motion",
        headline = "Also between your fingers",
    ),
    val bookToUpdate: Book? = null,
    val progressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,

    /** The reader's uncommitted time entry; `null` seeds the sheet's fields from the book. */
    val progressTimeDraft: ProgressTimeDraft? = null,
    val showProgressSheet: Boolean = false,

    /** The reader's backdate for the next logged action; `null` is "just now". */
    val readAt: LocalDateTime? = null,

    val progressSheet: ProgressSheetUiModel? = null,

    val deadlines: Map<Int, BookDeadline> = emptyMap(),
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,

    val failedMutationBookIds: Set<Int> = emptySet(),
    val dismissedPlanTodayByBook: Map<Int, String> = emptyMap(),

    /** `null` until the want-to-read shelf first loads; empty means it loaded with nothing on it. */
    val wantToReadBooks: List<Book>? = null,
    val trendingBooks: List<Book> = emptyList(),

    val pickUpNextHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "Pick up next"),
    val trendingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "Trending now"),
    val emptyCurrentlyReading: EmptyStateUiModel = EmptyStateUiModel(
        title = "An open page awaits",
        body = "Nothing is currently between your fingers. Find a title worth losing an evening to.",
        variant = EmptyStateVariant.Centered(
            button = EmptyStateButtonUiModel(
                label = "Find a book",
                icon = SoftcoverIcon.Search,
            ),
        ),
    ),

    val recentReadingActivity: List<ReadingDayActivity> = emptyList(),
    val streakEnabled: Boolean = true,

    val streakSheet: SheetScaffoldUiModel = streakSheetScaffoldFor(activity = emptyList()),

    /** The featured book's pace line, mapped by `ReadingPaceForecastCollector`; null while no forecast exists. */
    val featuredBookPace: String? = null,

    val verdictPromptBook: Book? = null,

    /** `null` exactly when [verdictPromptBook] is. */
    val verdictSheet: VerdictSheetUiModel? = null,

    val featuredBackdropCover: CoverUiModel? = null,
    val featuredCover: CoverUiModel? = null,
    val trendingTileCover: CoverUiModel? = null,
    val verdictCover: CoverUiModel? = null,

    /**
     * [alsoReading] holds every book but the featured first. Its rows are loading stand-ins while
     * [books] is empty, and [pickUpNext]'s tiles while [wantToReadBooks] is `null`.
     */
    val alsoReading: BookCardSet<Book> = BookCardSet.standIns(ALSO_READING_STAND_IN_CARDS),
    val pickUpNext: BookCardSet<Book> = BookCardSet.standIns(PICK_UP_NEXT_STAND_IN_CARDS),

    val deadlineCoverOverlays: Map<Int, CoverOverlayUiModel> = emptyMap(),

    /**
     * The featured card's progress readout, mapped by `FeaturedProgressCollector` from the featured
     * book ([books] `.firstOrNull()`); the masked stand-in while there is none.
     */
    val featuredProgress: FeaturedProgressUiModel = FeaturedProgressUiModel.Loading,

    /**
     * The featured book's ([books] `.firstOrNull()`) deadline summary line, in the
     * [nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryTone.OnHeroBackdrop] ink for the
     * blurred-cover hero backdrop, where the book cards' summaries take
     * [nl.rhaydus.softcover.core.component.deadline.DeadlineSummaryTone.OnSurface].
     */
    val featuredDeadlineSummary: DeadlineSummaryUiModel? = null,

    /**
     * `null` only until the first nudge applies. After that a dismissed or lapsed nudge keeps its last
     * message with `visible = false`, so the banner can animate out.
     */
    val featuredPaceNudge: BannerUiModel? = null,

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
) : UiState {
    /** The first of [books], or the loading stand-in while there are none. */
    val featuredBook: Book
        get() = books.firstOrNull() ?: FEATURED_STAND_IN_BOOK

    val isLoadingFeatured: Boolean
        get() = books.isEmpty()
}
