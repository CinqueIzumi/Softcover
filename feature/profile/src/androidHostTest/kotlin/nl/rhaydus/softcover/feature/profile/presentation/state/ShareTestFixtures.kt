package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toReadingLifeShareCardUiModel

internal fun buildShareReadingStats(): ReadingStats = ReadingStats()

internal fun buildShareReadingHistory(): ReadingHistory = ReadingHistory(trackedYears = 3)

internal fun buildShareProfile(name: String = "Jane Doe"): UserProfileData =
    UserProfileData(
        profileImageUrl = "https://example.com/avatar.png",
        name = name,
        username = "cinque",
        bio = "Avid reader",
        readingStreak = 7,
    )

internal fun buildShareCard(name: String = "Jane Doe"): ReadingLifeShareCardUiModel =
    buildShareReadingStats().toReadingLifeShareCardUiModel(
        history = buildShareReadingHistory(),
        profile = buildShareProfile(name = name),
    )
