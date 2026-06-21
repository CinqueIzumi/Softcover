package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

class RefreshUserProfileDataUseCase(
    private val profileRepository: ProfileRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId = getUserIdUseCase().getOrThrow()
        val snapshot = profileRepository.fetchUserProfileSnapshot(userId = userId)
        val data = snapshot.toUserProfileData(today = clock.todayIn(timeZone))

        profileRepository.cacheUserProfileData(data = data)
    }

    private fun UserProfileSnapshot.toUserProfileData(today: LocalDate): UserProfileData {
        // The streak is computed from the full fetched history, but only the strip's
        // window of dates is persisted — long streaks never need the old dates kept.
        val windowStart = today.minus(
            READING_ACTIVITY_WINDOW_DAYS - 1,
            DateTimeUnit.DAY,
        )
        val windowedDates = activeReadingDates
            .filterTo(mutableSetOf()) { it >= windowStart && it <= today }

        return UserProfileData(
            profileImageUrl = profileImageUrl,
            name = name,
            username = username,
            bio = bio,
            booksRead = booksRead,
            totalPagesRead = totalPagesRead,
            averageRating = averageRating,
            readingStreak = computeStreak(
                activeReadingDates,
                today,
            ),
            activeReadingDates = windowedDates,
        )
    }

    // Grace day: a user who hasn't logged yet today should not see their streak break,
    // so we start counting from yesterday when today has no entry. The streak only ends
    // once we hit a day with no progress/finished journal event.
    private fun computeStreak(
        activeDates: Set<LocalDate>,
        today: LocalDate,
    ): Int {
        var cursor = if (today in activeDates) today else today.minus(
            1,
            DateTimeUnit.DAY,
        )
        var streak = 0
        while (cursor in activeDates) {
            streak++
            cursor = cursor.minus(
                1,
                DateTimeUnit.DAY,
            )
        }
        return streak
    }
}
