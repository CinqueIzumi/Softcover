package nl.rhaydus.softcover.feature.profile.presentation.screen

import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.demographicChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genderChartUiModel
import nl.rhaydus.softcover.feature.profile.presentation.mapper.genreRankingUiModel
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
    authorDemographics = PreviewReadingLife.authorDemographics,
    genreRanking = genreRankingUiModel(PreviewReadingLife.genres),
    genderChart = genderChartUiModel(PreviewReadingLife.authorDemographics.genderSlices),
    bipocChart = demographicChartUiModel(PreviewReadingLife.authorDemographics.bipocBreakdown),
    lgbtqChart = demographicChartUiModel(PreviewReadingLife.authorDemographics.lgbtqBreakdown),
)
