package nl.rhaydus.softcover.feature.profile.presentation.state

import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.chart.ColumnChartUiModel
import nl.rhaydus.softcover.core.component.chart.ProportionChartUiModel
import nl.rhaydus.softcover.core.component.chart.RankedBarsUiModel
import nl.rhaydus.softcover.core.component.control.SegmentedControlUiModel
import nl.rhaydus.softcover.core.component.control.ToggleRowUiModel
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.statistic.StatNumberUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.RECENTLY_LOVED_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsAverageUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ratingsChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.readingAtlasUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRatingsHistogramHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearHistoryChartUiModel
import nl.rhaydus.toad.UiState

internal data class ProfileUiState(
    val userProfileData: UserProfileData? = null,
    val readingLife: ReadingLife? = null,
    val readingLifeShareCard: ReadingLifeShareCardUiModel? = null,
    val recentlyLoved: BookCardSet<LovedBook> = BookCardSet.standIns(RECENTLY_LOVED_STAND_IN_CARDS),
    val hideUntaggedAuthors: Boolean = false,

    /** Mapped by \`HideUntaggedAuthorsCollector\` alongside [hideUntaggedAuthors]. */
    val hideUntaggedAuthorsToggle: ToggleRowUiModel<String> = hideUntaggedAuthorsToggleFor(hide = false),
    val selectedYearMetric: YearMetric = YearMetric.BOOKS,

    /** Mapped by \`YearMetricToggleCollector\` off [selectedYearMetric] and whether the reading life has loaded. */
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
    val isLoading: Boolean = true,

    val readingAtlas: ReadingAtlasUiModel = readingAtlasUiModel(null),

    /**
     * The author breakdown the section's captions and empty checks read: every author by default, or
     * only the ones tagged for each attribute once [hideUntaggedAuthors] is on. Written by
     * `ProfileChartsCollector` together with the charts so the two never disagree.
     */
    val authorDemographics: AuthorDemographics = AuthorDemographics(),

    /** Mapped by `ProfileChartsCollector`, which also decides when they stop loading. */
    val genreRanking: RankedBarsUiModel = genreRankingUiModel(null),
    val genderChart: ProportionChartUiModel = genderChartUiModel(null),
    val bipocChart: ProportionChartUiModel = demographicChartUiModel(null),
    val lgbtqChart: ProportionChartUiModel = demographicChartUiModel(null),
    val ratingsChart: ColumnChartUiModel = ratingsChartUiModel(null),
    val ratingsAverage: StatNumberUiModel = ratingsAverageUiModel(null),

    val readingAtlasHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "Reading atlas"),
    val theReaderHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(eyebrow = "The reader"),
    val authorRepresentationHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Who you read",
        headline = "The authors behind your shelf",
    ),
    val genreRankingHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "What you reach for",
        headline = "The genres you read most",
    ),
    val recentlyLovedHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "Recently loved",
        headline = "The last few that landed",
    ),
    val yearColumnHistoryHeader: SectionHeaderUiModel = SectionHeaderUiModel.Section(
        eyebrow = "The shape of the years",
        headline = "Reading, year by year",
    ),
    val ratingsHistogramHeader: SectionHeaderUiModel = RatingBand.NEW_READER.toRatingsHistogramHeader(),
    val ratingsHistogramCaption: String = RatingBand.NEW_READER.toRatingsHistogramCaption(),
    val shareSheetScaffold: SheetScaffoldUiModel = profileShareSheetScaffold(),
    val logOutSheetScaffold: SheetScaffoldUiModel = logOutSheetScaffold(),

    /** Both bars carry no title — the page's own masthead names it. */
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
     * *unfiltered* data on purpose: [authorDemographics] has already zeroed those buckets while the
     * switch is on, so deriving it from that would make the switch disappear the moment it was used.
     */
    val hasUntaggedAuthors: Boolean
        get() = with(allAuthorDemographics) {
            unknownGenderCount > 0 ||
                bipocBreakdown.unknownCount > 0 ||
                lgbtqBreakdown.unknownCount > 0
        }
}
