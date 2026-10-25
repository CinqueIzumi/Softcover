package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_KEY
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_LINE
import nl.rhaydus.softcover.feature.profile.presentation.mapper.COLLAPSED_GENRE_COUNT
import nl.rhaydus.softcover.feature.profile.presentation.mapper.RECENTLY_LOVED_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.profile.presentation.mapper.TIME_OF_DAY_FOOTNOTE
import nl.rhaydus.softcover.feature.profile.presentation.mapper.TIME_OF_DAY_TITLE
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasChapterHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasContentsHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasContentsRows
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasOpeningUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.authorsEmptyCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.authorsScopeCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreEmptyCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasMonthlyPages
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasTimeOfDayPages
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingGenreHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingMonthlyPagesChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingMonthlyPagesHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingTimeOfDayCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingTimeOfDayChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.loadingYearAtAGlanceHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesMonthSetKey
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsEmptyCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryChartUiModel
import nl.rhaydus.toad.UiState

internal data class StatsAtlasUiState(
    val userProfileData: UserProfileData? = null,
    val readingStats: ReadingStats? = null,
    val readingHistory: ReadingHistory? = null,
    val isLoading: Boolean = true,

    /** Built by `AtlasScopeChipsCollector`; the chip keyed [selectedScopeKey] is the selected one. */
    val scopeChips: ChipSet<StatsScope> = atlasScopeChips(
        yearsRead = emptyList(),
        selectedKey = ALL_TIME_SCOPE_KEY,
    ),
    val selectedScopeKey: String = ALL_TIME_SCOPE_KEY,

    /** Mapped by `AtlasScopedSectionsCollector` together with the sections it describes. */
    val scopeLine: String = ALL_TIME_SCOPE_LINE,
    val recentlyLoved: BookCardSet<RecentlyLovedEntry> = BookCardSet.standIns(RECENTLY_LOVED_STAND_IN_CARDS),
    val hideUntaggedAuthors: Boolean = false,

    /** Mapped by `HideUntaggedAuthorsCollector` alongside [hideUntaggedAuthors]. */
    val hideUntaggedAuthorsToggle: ToggleRowUiModel<String> = hideUntaggedAuthorsToggleFor(hide = false),
    val selectedYearMetric: YearMetric = YearMetric.BOOKS,

    /** Mapped by `YearMetricToggleCollector` off [selectedYearMetric] and whether the reading history has loaded. */
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

    /** Mapped by `GenreRankingCollector` off [readingStats], [genreRankingShownCount] and [genreEmptyCaption]. */
    val genreHeader: SectionHeaderUiModel.Section = loadingGenreHeader(),
    val genreRanking: RankedBarsUiModel = genreRankingUiModel(
        slices = null,
        shownCount = COLLAPSED_GENRE_COUNT,
    ),
    val genreRankingShowMore: PillButtonUiModel? = null,
    val genreRankingShowLess: PillButtonUiModel? = null,
    val genreRankingShownCount: Int = COLLAPSED_GENRE_COUNT,

    /** Mapped by `AtlasScopedSectionsCollector` together with the stats it describes. */
    val genreEmptyCaption: String = genreEmptyCaption(booksRead = 0),

    /**
     * Every author by default, or only the ones tagged for each attribute once [hideUntaggedAuthors]
     * is on.
     */
    val genderBreakdown: DemographicBreakdownUiModel = genderBreakdownUiModel(
        demographics = null,
        hideUntaggedAuthors = false,
    ),
    val bipocBreakdown: DemographicBreakdownUiModel = demographicBreakdownUiModel(
        breakdown = null,
        hideUntaggedAuthors = false,
    ),
    val lgbtqBreakdown: DemographicBreakdownUiModel = demographicBreakdownUiModel(
        breakdown = null,
        hideUntaggedAuthors = false,
    ),
    val authorsScopeCaption: String = authorsScopeCaption(hideUntaggedAuthors = false),
    val authorsEmptyCaption: String = authorsEmptyCaption(
        hideUntaggedAuthors = false,
        booksRead = 0,
    ),
    val ratingsChart: ColumnChartUiModel = ratingsChartUiModel(null),
    val ratingsAverage: StatNumberUiModel = ratingsAverageUiModel(null),

    val ratingsEmptyCaption: String = ratingsEmptyCaption(booksRead = 0),

    /** Mapped by `AtlasScopedSectionsCollector`. */
    val yearAtAGlanceHeader: SectionHeaderUiModel = loadingYearAtAGlanceHeader(),
    val timeOfDayTitle: String = TIME_OF_DAY_TITLE,
    val timeOfDayCaption: String = loadingTimeOfDayCaption(),
    val timeOfDayChart: ColumnChartUiModel = loadingTimeOfDayChartUiModel(),
    val timeOfDayFootnote: String = TIME_OF_DAY_FOOTNOTE,
    val hasTimeOfDayPages: Boolean = hasTimeOfDayPages(null),

    /** Mapped by `AtlasScopedSectionsCollector`; [monthlyPagesMonthSetKey] is null while loading. */
    val monthlyPagesHeader: SectionHeaderUiModel = loadingMonthlyPagesHeader(),
    val monthlyPagesChart: ColumnChartUiModel = loadingMonthlyPagesChartUiModel(),
    val hasMonthlyPages: Boolean = hasMonthlyPages(null),
    val monthlyPagesMonthSetKey: String? = monthlyPagesMonthSetKey(null),

    /** Mapped by `AtlasScopedSectionsCollector` off [readingStats]. */
    val opening: AtlasOpeningUiModel = atlasOpeningUiModel(null),
    val contentsHeader: SectionHeaderUiModel = atlasContentsHeader(),
    val contentsRows: ImmutableList<AtlasContentsRowUiModel> = atlasContentsRows(),
    val temporaryChapterHeader: SectionHeaderUiModel = atlasChapterHeader(AtlasChapter.TEMPORARY),
    val authorRepresentationHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Who you read",
        headline = "Who you've been reading",
    ),
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
    private val allAuthorDemographics: AuthorDemographics
        get() = readingStats?.authorDemographics ?: AuthorDemographics()

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

    /**
     * Whether any of the three breakdowns actually has an untagged bucket to drop. Reads the
     * *unfiltered* data on purpose: the breakdown models have already zeroed those buckets while the
     * switch is on, so deriving it from that would make the switch disappear the moment it was used.
     */
    val hasUntaggedAuthors: Boolean
        get() = with(allAuthorDemographics) {
            unknownGenderCount > 0 ||
                bipocBreakdown.unknownCount > 0 ||
                lgbtqBreakdown.unknownCount > 0
        }
}
