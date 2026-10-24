package nl.rhaydus.softcover.feature.profile.presentation.screen

import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.ALL_TIME_SCOPE_KEY
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasOpeningUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.atlasScopeChips
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreScopeCaption
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingHistoryPreview
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingStatsPreview
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState

private val PreviewReadingStats = profileReadingStatsPreview()
private val PreviewReadingHistory = profileReadingHistoryPreview()

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
