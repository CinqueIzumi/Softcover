package nl.rhaydus.softcover.feature.profile.presentation.state

import nl.rhaydus.softcover.core.component.share.ReadingLifeShareCardUiModel
import nl.rhaydus.softcover.core.profile.domain.model.PagesRead
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toReadingLifeShareCardUiModel

internal fun buildShareReadingStats(): ReadingStats = ReadingStats()

internal fun buildSharePagesRead(): PagesRead = PagesRead(
    total = 0,
    finishedBookCount = 0,
    stoppedBookCount = 0,
    hasInProgressPages = false,
    pagesPerDay = null,
)

internal fun buildShareReadingHistory(): ReadingHistory = ReadingHistory(trackedYears = 3)

internal fun buildShareProfile(name: String = "Jane Doe"): UserProfileData = UserProfileData(
    profileImageUrl = "https://example.com/avatar.png",
    name = name,
    username = "cinque",
    bio = "Avid reader",
)

internal fun buildShareCard(
    name: String = "Jane Doe",
    dayStreak: Int = 0,
): ReadingLifeShareCardUiModel = buildShareReadingStats().toReadingLifeShareCardUiModel(
    pagesRead = buildSharePagesRead(),
    history = buildShareReadingHistory(),
    profile = buildShareProfile(name = name),
    dayStreak = dayStreak,
)
