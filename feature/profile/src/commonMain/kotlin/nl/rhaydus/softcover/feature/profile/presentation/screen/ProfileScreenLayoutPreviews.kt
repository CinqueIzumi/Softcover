package nl.rhaydus.softcover.feature.profile.presentation.screen

import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderBreakdownUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreScopeCaption
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingLifePreview
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

private val PreviewReadingLife = profileReadingLifePreview()

internal val ProfileScreenPreviewState = ProfileUiState(
    isLoading = false,
    userProfileData = UserProfileData(
        profileImageUrl = "",
        name = "Elena Marchetti",
        username = "elenam",
        bio = "Mostly literary fiction, the occasional doorstop fantasy.",
        booksRead = 214,
        totalPagesRead = 128_406,
        averageRating = 4.2,
        readingStreak = 31,
    ),
    readingLife = PreviewReadingLife,
    genreRanking = genreRankingUiModel(PreviewReadingLife.genres),
    hasGenres = true,
    genreScopeCaption = genreScopeCaption(taggedBookCount = PreviewReadingLife.genres.taggedBookCount),
    genderBreakdown = genderBreakdownUiModel(
        demographics = PreviewReadingLife.authorDemographics,
        hideUntaggedAuthors = false,
    ),
    bipocBreakdown = demographicBreakdownUiModel(
        breakdown = PreviewReadingLife.authorDemographics.bipocBreakdown,
        hideUntaggedAuthors = false,
    ),
    lgbtqBreakdown = demographicBreakdownUiModel(
        breakdown = PreviewReadingLife.authorDemographics.lgbtqBreakdown,
        hideUntaggedAuthors = false,
    ),
)
