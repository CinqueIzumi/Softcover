package nl.rhaydus.softcover.feature.profile.presentation.screen

import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.readingSummaryUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingHistoryPreview
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingStatsPreview
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

private val PreviewReadingStats = profileReadingStatsPreview()
private val PreviewReadingHistory = profileReadingHistoryPreview()

internal val ProfileScreenPreviewState = ProfileUiState(
    isLoading = false,
    userProfileData = UserProfileData(
        profileImageUrl = "",
        name = "Elena Marchetti",
        username = "elenam",
        bio = "Mostly literary fiction, the occasional doorstop fantasy.",
        readingStreak = 31,
    ),
    readingStats = PreviewReadingStats,
    readingHistory = PreviewReadingHistory,
    readingSummary = readingSummaryUiModel(
        history = PreviewReadingHistory,
        currentYear = 2025,
    ),
)
