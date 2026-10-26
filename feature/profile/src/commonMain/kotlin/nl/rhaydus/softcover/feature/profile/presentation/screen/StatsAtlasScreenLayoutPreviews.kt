package nl.rhaydus.softcover.feature.profile.presentation.screen

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_KEY
import nl.rhaydus.softcover.feature.profile.presentation.mapper.COLLAPSED_GENRE_COUNT
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasOpeningUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreEmptyCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingShowLess
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingShowMore
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasTimeOfDayPages
import nl.rhaydus.softcover.feature.profile.presentation.mapper.hasTopAuthors
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesMonthSetKey
import nl.rhaydus.softcover.feature.profile.presentation.mapper.timeOfDayCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.timeOfDayChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.topAuthorRows
import nl.rhaydus.softcover.feature.profile.presentation.mapper.topAuthorsCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.yearAtAGlanceHeader
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileHourlyPagesPreview
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileMonthlyPagesPreview
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingHistoryPreview
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingStatsPreview
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState

private val PreviewReadingStats = profileReadingStatsPreview()
private val PreviewReadingHistory = profileReadingHistoryPreview()
private val PreviewMonthlyPages = profileMonthlyPagesPreview()
private val PreviewHourlyPages = profileHourlyPagesPreview()

private val PreviewToday = LocalDate(
    year = 2026,
    month = 3,
    day = 14,
)

private val PreviewProfileData = UserProfileData(
    profileImageUrl = "",
    name = "Elena Marchetti",
    username = "elenam",
    bio = "Mostly literary fiction, the occasional doorstop fantasy.",
    readingStreak = 31,
)

internal val StatsAtlasPreviewState = StatsAtlasUiState(
    isLoading = false,
    userProfileData = PreviewProfileData,
    opening = atlasOpeningUiModel(PreviewReadingStats),
    readingStats = PreviewReadingStats,
    readingHistory = PreviewReadingHistory,
    scopeChips = atlasScopeChips(
        yearsRead = PreviewReadingHistory.yearsRead,
        selectedKey = ALL_TIME_SCOPE_KEY,
    ),
    yearAtAGlanceHeader = yearAtAGlanceHeader(
        scope = StatsScope.AllTime,
        today = PreviewToday,
    ),
    timeOfDayCaption = timeOfDayCaption(
        hourly = PreviewHourlyPages,
        scope = StatsScope.AllTime,
    ),
    timeOfDayChart = timeOfDayChartUiModel(PreviewHourlyPages),
    hasTimeOfDayPages = hasTimeOfDayPages(PreviewHourlyPages),
    monthlyPagesHeader = monthlyPagesHeader(
        monthlyPages = PreviewMonthlyPages,
        scope = StatsScope.AllTime,
        today = PreviewToday,
    ),
    monthlyPagesChart = monthlyPagesChartUiModel(
        monthlyPages = PreviewMonthlyPages,
        scope = StatsScope.AllTime,
        today = PreviewToday,
    ),
    hasMonthlyPages = true,
    monthlyPagesMonthSetKey = monthlyPagesMonthSetKey(PreviewMonthlyPages),
    topAuthorsCaption = topAuthorsCaption(
        topAuthors = PreviewReadingStats.topAuthors,
        booksRead = PreviewReadingStats.booksRead,
        scope = StatsScope.AllTime,
    ),
    topAuthorRows = topAuthorRows(PreviewReadingStats.topAuthors),
    hasTopAuthors = hasTopAuthors(PreviewReadingStats.topAuthors),
    genreHeader = genreHeader(
        slices = PreviewReadingStats.genres.slices,
        emptyCaption = genreEmptyCaption(booksRead = PreviewReadingStats.booksRead),
    ),
    genreRanking = genreRankingUiModel(
        slices = PreviewReadingStats.genres.slices,
        shownCount = COLLAPSED_GENRE_COUNT,
    ),
    genreRankingShowMore = genreRankingShowMore(
        slices = PreviewReadingStats.genres.slices,
        shownCount = COLLAPSED_GENRE_COUNT,
    ),
    genreRankingShowLess = genreRankingShowLess(
        slices = PreviewReadingStats.genres.slices,
        shownCount = COLLAPSED_GENRE_COUNT,
    ),
    genderBreakdown = genderBreakdownUiModel(
        demographics = PreviewReadingStats.authorDemographics,
        hideUntaggedAuthors = false,
    ),
    bipocBreakdown = demographicBreakdownUiModel(
        breakdown = PreviewReadingStats.authorDemographics.bipocBreakdown,
        hideUntaggedAuthors = false,
    ),
    lgbtqBreakdown = demographicBreakdownUiModel(
        breakdown = PreviewReadingStats.authorDemographics.lgbtqBreakdown,
        hideUntaggedAuthors = false,
    ),
)
