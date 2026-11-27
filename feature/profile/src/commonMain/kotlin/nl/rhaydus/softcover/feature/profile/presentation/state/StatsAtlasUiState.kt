package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.chart.YearGridUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.statistic.StatTileUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_KEY
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_LINE
import nl.rhaydus.softcover.feature.profile.presentation.mapper.COLLAPSED_GENRE_COUNT
import nl.rhaydus.softcover.feature.profile.presentation.mapper.FORMAT_SPLIT_TITLE
import nl.rhaydus.softcover.feature.profile.presentation.mapper.LOADING_TODAY
import nl.rhaydus.softcover.feature.profile.presentation.mapper.LOADING_YEAR
import nl.rhaydus.softcover.feature.profile.presentation.mapper.RECENTLY_LOVED_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.profile.presentation.mapper.TIME_OF_DAY_FOOTNOTE
import nl.rhaydus.softcover.feature.profile.presentation.mapper.TIME_OF_DAY_TITLE
import nl.rhaydus.softcover.feature.profile.presentation.mapper.TOP_AUTHORS_TITLE
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasChapterHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasContentsHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasContentsRows
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasOpeningUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.currentStreakTile
import nl.rhaydus.softcover.feature.profile.presentation.mapper.diversityChapterUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.formatSplitCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.formatSplitChart
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreEmptyCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasFormatSplit
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasMonthlyPages
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasTimeOfDayPages
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasTopAuthors
import nl.rhaydus.softcover.feature.profile.presentation.mapper.inYourWordsUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingGenreHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingMonthlyPagesChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingMonthlyPagesHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingTimeOfDayCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingTimeOfDayChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingYearAtAGlanceHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingYearGrid
import nl.rhaydus.softcover.feature.profile.presentation.mapper.longestStreakTile
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesMonthSetKey
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsEmptyCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.readingRecordsUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.timeToFinishUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.topAuthorRows
import nl.rhaydus.softcover.feature.profile.presentation.mapper.topAuthorsCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.waitingLongestUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yourReadingListUiModel
import nl.rhaydus.toad.UiState

internal data class StatsAtlasUiState(
    val userProfileData: UserProfileData? = null,
    val readingStats: ReadingStats? = null,
    val readingHistory: ReadingHistory? = null,
    val isLoading: Boolean = true,

    val scopeChips: ChipSet<StatsScope> = atlasScopeChips(
        yearsRead = emptyList(),
        selectedKey = ALL_TIME_SCOPE_KEY,
    ),
    val selectedScopeKey: String = ALL_TIME_SCOPE_KEY,

    val scopeLine: String = ALL_TIME_SCOPE_LINE,
    val recentlyLoved: BookCardSet<RecentlyLovedEntry> = BookCardSet.standIns(RECENTLY_LOVED_STAND_IN_CARDS),
    val hideBooksWithoutDetail: Boolean = false,
    val editionLanguagesPending: Boolean = true,

    val hideBooksWithoutDetailToggle: ToggleRowUiModel<String> = hideBooksWithoutDetailToggleFor(hide = false),
    val selectedYearMetric: YearMetric = YearMetric.BOOKS,

    val yearMetricToggle: SegmentedControlUiModel<YearMetric> = yearMetricToggleFor(
        selected = YearMetric.BOOKS,
        enabled = false,
    ),

    val yearHistoryChart: ColumnChartUiModel = yearHistoryChartUiModel(
        series = null,
        metric = YearMetric.BOOKS,
    ),
    val yearHistoryCaption: String = yearHistoryCaption(
        series = null,
        metric = YearMetric.BOOKS,
    ),

    val genreHeader: SectionHeaderUiModel.Section = loadingGenreHeader(),
    val genreRanking: RankedBarsUiModel = genreRankingUiModel(
        slices = null,
        shownCount = COLLAPSED_GENRE_COUNT,
    ),
    val genreRankingShowMore: PillButtonUiModel? = null,
    val genreRankingShowLess: PillButtonUiModel? = null,
    val genreRankingShownCount: Int = COLLAPSED_GENRE_COUNT,

    val genreEmptyCaption: String = genreEmptyCaption(booksRead = 0),

    val diversityChapter: DiversityChapterUiModel = diversityChapterUiModel(
        stats = null,
        hideBooksWithoutDetail = false,
    ),
    val ratingsChart: ColumnChartUiModel = ratingsChartUiModel(null),
    val ratingsAverage: StatNumberUiModel = ratingsAverageUiModel(null),

    val ratingsEmptyCaption: String = ratingsEmptyCaption(booksRead = 0),

    val yearAtAGlanceHeader: SectionHeaderUiModel = loadingYearAtAGlanceHeader(),
    val yearGrid: YearGridUiModel = loadingYearGrid(),
    val currentStreakTile: StatTileUiModel = currentStreakTile(
        streak = null,
        today = LOADING_TODAY,
        isLoading = true,
    ),
    val longestStreakTile: StatTileUiModel = longestStreakTile(
        streak = null,
        year = LOADING_YEAR,
        isLoading = true,
    ),
    val atlasStreaks: AtlasStreaks = AtlasStreaks(),
    val selectedReadingDay: LocalDate? = null,

    val dayDetailSheet: SheetSession<DayDetailUiModel, DayDetailBook> = SheetSession.Closed,
    val timeOfDayTitle: String = TIME_OF_DAY_TITLE,
    val timeOfDayCaption: String = loadingTimeOfDayCaption(),
    val timeOfDayChart: ColumnChartUiModel = loadingTimeOfDayChartUiModel(),
    val timeOfDayFootnote: String = TIME_OF_DAY_FOOTNOTE,
    val hasTimeOfDayPages: Boolean = hasTimeOfDayPages(null),

    /** [monthlyPagesMonthSetKey] is null while loading. */
    val monthlyPagesHeader: SectionHeaderUiModel = loadingMonthlyPagesHeader(),
    val monthlyPagesChart: ColumnChartUiModel = loadingMonthlyPagesChartUiModel(),
    val hasMonthlyPages: Boolean = hasMonthlyPages(null),
    val monthlyPagesMonthSetKey: String? = monthlyPagesMonthSetKey(null),

    val topAuthorsTitle: String = TOP_AUTHORS_TITLE,
    val topAuthorsCaption: String = topAuthorsCaption(
        topAuthors = null,
        booksRead = 0,
        scope = StatsScope.AllTime,
    ),
    val topAuthorRows: ImmutableList<BookCardUiModel> = topAuthorRows(null),
    val hasTopAuthors: Boolean = hasTopAuthors(null),

    val formatSplitTitle: String = FORMAT_SPLIT_TITLE,
    val formatSplitCaption: String = formatSplitCaption(
        split = null,
        booksRead = 0,
        scope = StatsScope.AllTime,
    ),
    val formatSplitChart: ProportionChartUiModel = formatSplitChart(null),
    val hasFormatSplit: Boolean = hasFormatSplit(null),

    val timeToFinish: TimeToFinishUiModel = timeToFinishUiModel(
        timeToFinish = null,
        scope = StatsScope.AllTime,
    ),

    val readingRecords: ReadingRecordsUiModel = readingRecordsUiModel(
        stats = null,
        timeToFinish = null,
        scope = StatsScope.AllTime,
    ),

    val yourReadingList: YourReadingListUiModel = yourReadingListUiModel(
        stats = null,
        booksRead = null,
        didNotFinish = null,
        scope = StatsScope.AllTime,
    ),

    val waitingLongest: WaitingLongestUiModel = waitingLongestUiModel(longest = null),

    val inYourWords: InYourWordsUiModel = inYourWordsUiModel(
        written = null,
        ownTags = null,
        scope = StatsScope.AllTime,
    ),

    val opening: AtlasOpeningUiModel = atlasOpeningUiModel(null),
    val contentsHeader: SectionHeaderUiModel = atlasContentsHeader(),
    val contentsRows: ImmutableList<AtlasContentsRowUiModel> = atlasContentsRows(),
    val temporaryChapterHeader: SectionHeaderUiModel = atlasChapterHeader(AtlasChapter.TEMPORARY),
    val recentlyLovedHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Recently loved",
        headline = "Recent favourites",
    ),
    val yearColumnHistoryHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Over the years",
        headline = "Reading, year by year",
    ),
    val ratingsHistogramHeader: SectionHeaderUiModel = RatingBand.NEW_READER.toRatingsHistogramHeader(),
    val ratingsHistogramCaption: String = RatingBand.NEW_READER.toRatingsHistogramCaption(),
    val ratingsAverageLabel: String = ratingsAverageCaption(totalRatings = 0),
    val masthead: PageMastheadUiModel = PageMastheadUiModel(
        title = "Stats",
        eyebrow = "Your reading record",
        subtitle = PageMastheadSubtitle.Text("A long read of your reading."),
    ),
    val topBar: TopBarUiModel = TopBarUiModel(),
) : UiState {
    /** The scope [selectedScopeKey] stands for; every year until the chip set holds it. */
    val selectedScope: StatsScope
        get() = scopeChips[selectedScopeKey] ?: StatsScope.AllTime

    val isReadingLifeLoading: Boolean
        get() = isLoading || readingStats == null || readingHistory == null

    val ratingsTotal: Int
        get() = readingStats?.ratings?.totalRatings ?: 0

    /**
     * Whether the reader has any loved reads to show. Read from [readingHistory] rather than the mapped
     * [recentlyLoved], which land a frame later and would flash the empty copy.
     */
    val hasRecentlyLoved: Boolean
        get() = readingHistory?.recentlyLoved.orEmpty().isNotEmpty()
}
