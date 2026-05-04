package nl.rhaydus.softcover.feature.profile.domain.usecase

import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.feature.profile.domain.repository.ProfileRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
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

    private fun UserProfileSnapshot.toUserProfileData(today: LocalDate): UserProfileData =
        UserProfileData(
            profileImageUrl = profileImageUrl,
            name = name,
            bio = bio,
            booksRead = booksRead,
            totalPagesRead = totalPagesRead,
            averageRating = averageRating,
            readingStreak = computeStreak(activeReadingDates, today),
        )

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
