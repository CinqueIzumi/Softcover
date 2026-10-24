package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.header.PageMastheadSubtitle
import nl.rhaydus.softcover.core.component.header.PageMastheadUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.RECENTLY_LOVED_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasChapterHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasContentsHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasContentsRows
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasOpeningUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreScopeCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryChartUiModel
import nl.rhaydus.toad.UiState

internal data class StatsAtlasUiState(
    val userProfileData: UserProfileData? = null,
    val readingLife: ReadingLife? = null,
    val isLoading: Boolean = true,
    val recentlyLoved: BookCardSet<RecentlyLovedEntry> = BookCardSet.standIns(RECENTLY_LOVED_STAND_IN_CARDS),
    val hideUntaggedAuthors: Boolean = false,

    /** Mapped by `HideUntaggedAuthorsCollector` alongside [hideUntaggedAuthors]. */
    val hideUntaggedAuthorsToggle: ToggleRowUiModel<String> = hideUntaggedAuthorsToggleFor(hide = false),
    val selectedYearMetric: YearMetric = YearMetric.BOOKS,

    /** Mapped by `YearMetricToggleCollector` off [selectedYearMetric] and whether the reading life has loaded. */
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

    /** Mapped by `AtlasChartsCollector`, which also decides when they stop loading. */
    val genreRanking: RankedBarsUiModel = genreRankingUiModel(null),
    val hasGenres: Boolean = false,
    val genreScopeCaption: String = genreScopeCaption(taggedBookCount = 0),

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
    val ratingsChart: ColumnChartUiModel = ratingsChartUiModel(null),
    val ratingsAverage: StatNumberUiModel = ratingsAverageUiModel(null),

    /** Mapped by `AtlasProfileDataCollector` off [userProfileData]. */
    val opening: AtlasOpeningUiModel = atlasOpeningUiModel(null),
    val contentsHeader: SectionHeaderUiModel = atlasContentsHeader(),
    val contentsRows: ImmutableList<AtlasContentsRowUiModel> = atlasContentsRows(),
    val temporaryChapterHeader: SectionHeaderUiModel = atlasChapterHeader(AtlasChapter.TEMPORARY),
    val authorRepresentationHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Who you read",
        headline = "Who you've been reading",
    ),
    val genreRankingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "What you reach for",
        headline = "The genres you read most",
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
        get() = readingLife?.authorDemographics ?: AuthorDemographics()

    val isReadingLifeLoading: Boolean
        get() = isLoading || readingLife == null

    val ratingsTotal: Int
        get() = readingLife?.ratings?.totalRatings ?: 0

    /**
     * Whether the reader has any loved reads to show. Read from [readingLife] rather than the mapped
     * [recentlyLoved], which land a frame later and would flash the empty copy.
     */
    val hasRecentlyLoved: Boolean
        get() = readingLife?.recentlyLoved.orEmpty().isNotEmpty()

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
