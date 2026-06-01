package nl.rhaydus.softcover.feature.profile.domain.usecase

import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.feature.profile.domain.repository.ProfileRepository
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import java.time.Clock
import java.time.LocalDate

class RefreshUserProfileDataUseCase(
    private val profileRepository: ProfileRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val clock: Clock,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId = getUserIdUseCase().getOrThrow()
        val snapshot = profileRepository.fetchUserProfileSnapshot(userId = userId)
        val data = snapshot.toUserProfileData(today = LocalDate.now(clock))

        profileRepository.cacheUserProfileData(data = data)
    }

    private fun UserProfileSnapshot.toUserProfileData(today: LocalDate): UserProfileData {
        // The streak is computed from the full fetched history, but only the strip's
        // window of dates is persisted — long streaks never need the old dates kept.
        val windowStart = today.minusDays((READING_ACTIVITY_WINDOW_DAYS - 1).toLong())
        val windowedDates = activeReadingDates
            .filterTo(mutableSetOf()) { it.isBefore(windowStart).not() && it.isAfter(today).not() }

        return UserProfileData(
            profileImageUrl = profileImageUrl,
            name = name,
            username = username,
            bio = bio,
            booksRead = booksRead,
            totalPagesRead = totalPagesRead,
            averageRating = averageRating,
            readingStreak = computeStreak(activeReadingDates, today),
            activeReadingDates = windowedDates,
        )
    }

    // Grace day: a user who hasn't logged yet today should not see their streak break,
    // so we start counting from yesterday when today has no entry. The streak only ends
    // once we hit a day with no progress/finished journal event.
    private fun computeStreak(activeDates: Set<LocalDate>, today: LocalDate): Int {
        var cursor = if (today in activeDates) today else today.minusDays(1)
        var streak = 0
        while (cursor in activeDates) {
            streak++
            cursor = cursor.minusDays(1)
        }
        return streak
    }
}
