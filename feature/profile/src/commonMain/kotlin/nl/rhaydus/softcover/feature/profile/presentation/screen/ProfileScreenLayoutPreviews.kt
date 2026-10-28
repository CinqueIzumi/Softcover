package nl.rhaydus.softcover.feature.profile.presentation.screen

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.readingSummaryUiModel
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingActivityPreview
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingHistoryPreview
import nl.rhaydus.softcover.feature.profile.presentation.screen.section.profileReadingStatsPreview
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState

private val PreviewReadingStats = profileReadingStatsPreview()
private val PreviewReadingHistory = profileReadingHistoryPreview()
private val PreviewReadingActivity = profileReadingActivityPreview()
private const val PREVIEW_STREAK_DAYS = 31

internal val ProfileScreenPreviewState = ProfileUiState(
    isLoading = false,
    userProfileData = UserProfileData(
        profileImageUrl = "",
        name = "Elena Marchetti",
        username = "elenam",
        bio = "Mostly literary fiction, the occasional doorstop fantasy.",
    ),
    readingStats = PreviewReadingStats,
    readingHistory = PreviewReadingHistory,
    readingSummary = readingSummaryUiModel(
        history = PreviewReadingHistory,
        activity = PreviewReadingActivity,
        today = LocalDate(
            year = 2025,
            month = 10,
            day = 27,
        ),
    ),
    currentStreakDays = PREVIEW_STREAK_DAYS,
)
