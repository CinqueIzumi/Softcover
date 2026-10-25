package nl.rhaydus.softcover.feature.profile.presentation.screen

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.domain.model.StatsScope
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_KEY
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasOpeningUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreScopeCaption
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesHeader
import nl.rhaydus.softcover.feature.profile.presentation.mapper.monthlyPagesMonthSetKey
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileMonthlyPagesPreview
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingHistoryPreview
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingStatsPreview
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState

private val PreviewReadingStats = profileReadingStatsPreview()
private val PreviewReadingHistory = profileReadingHistoryPreview()
private val PreviewMonthlyPages = profileMonthlyPagesPreview()

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
    genreRanking = genreRankingUiModel(PreviewReadingStats.genres),
    hasGenres = true,
    genreScopeCaption = genreScopeCaption(taggedBookCount = PreviewReadingStats.genres.taggedBookCount),
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
