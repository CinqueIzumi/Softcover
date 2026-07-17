package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlin.time.Clock
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.transformWhile
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

class RefreshUserProfileDataUseCase(
    private val profileRepository: ProfileRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val profileRefreshGate: ProfileRefreshGate,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    // Skips the network fetch once this session has already refreshed successfully - every other
    // Profile visit renders from the DataStore cache. runOnce guards check-fetch-mark atomically
    // and marks only after a successful cache write, so a transient failure leaves the gate open
    // and the next visit retries.
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        profileRefreshGate.runOnce {
            val userId = getUserIdUseCase().getOrThrow()
            val today = clock.todayIn(timeZone)
            val windowStart = today.minus(
                READING_ACTIVITY_WINDOW_DAYS - 1,
                DateTimeUnit.DAY,
            )
            val snapshot = profileRepository.fetchUserProfileSnapshot()
            val readingStreak = computeStreak(
                days = collectStreakDays(
                    userId = userId,
                    today = today,
                ),
                today = today,
            )
            val recentReadingDays = profileRepository.getActiveReadingDaysSince(
                userId = userId,
                since = windowStart,
            )
            val data = snapshot.toUserProfileData(
                readingStreak = readingStreak,
                recentReadingDays = recentReadingDays,
            )

            profileRepository.cacheUserProfileData(data = data)
        }
    }

    // Walks the descending reading-day stream newest-to-oldest, adding each date before
    // deciding whether to continue. transformWhile includes the deciding element itself (the
    // day that turns the gap from undetermined to confirmed) and then cancels the upstream
    // paging in ProfileRemoteDataSource, so a short streak never pages through the whole
    // history just to determine its length.
    private suspend fun collectStreakDays(
        userId: Int,
        today: LocalDate,
    ): Set<LocalDate> {
        val days = mutableSetOf<LocalDate>()

        profileRepository.streamReadingDaysDescending(userId = userId)
            .transformWhile { date ->
                days += date
                emit(date)
                hasUndeterminedStreakGap(
                    days = days,
                    today = today,
                )
            }
            .collect()

        return days
    }

    // The gap is undetermined while the consecutive run reaches all the way back to the oldest
    // date seen so far - once a real gap sits inside the fetched dates, the streak is fully
    // determined and older rows cannot change it.
    private fun hasUndeterminedStreakGap(
        days: Set<LocalDate>,
        today: LocalDate,
    ): Boolean {
        val oldest = days.min()
        val firstMissing = firstMissingDayWalkingBack(
            days = days,
            today = today,
        )

        return firstMissing < oldest
    }

    private fun UserProfileSnapshot.toUserProfileData(
        readingStreak: Int,
        recentReadingDays: Set<LocalDate>,
    ): UserProfileData = UserProfileData(
        profileImageUrl = profileImageUrl,
        name = name,
        username = username,
        bio = bio,
        booksRead = booksRead,
        totalPagesRead = totalPagesRead,
        averageRating = averageRating,
        readingStreak = readingStreak,
        recentReadingDays = recentReadingDays,
        booksByYear = booksByYear,
        pagesByYear = pagesByYear,
        pagesByMonth = pagesByMonth,
        genres = genres,
        ratings = ratings,
        recentlyLoved = recentlyLoved,
        trackedYears = trackedYears,
    )

    // Grace day: a user who hasn't logged yet today should not see their streak break, so the
    // walk-back starts from yesterday when today has no entry. Shared with the streak-gap
    // search so both use the identical grace-day start.
    private fun streakStartCursor(
        days: Set<LocalDate>,
        today: LocalDate,
    ): LocalDate = if (today in days) {
        today
    } else {
        today.minus(
            1,
            DateTimeUnit.DAY,
        )
    }

    // The streak only ends once we hit a day with no progress/finished journal event.
    private fun computeStreak(
        days: Set<LocalDate>,
        today: LocalDate,
    ): Int {
        var cursor = streakStartCursor(
            days,
            today,
        )

        var streak = 0

        while (cursor in days) {
            streak++
            cursor = cursor.minus(
                1,
                DateTimeUnit.DAY,
            )
        }

        return streak
    }

    // Walks back from the same grace-day start as computeStreak and returns the first day not
    // present in the set — i.e. where the streak breaks.
    private fun firstMissingDayWalkingBack(
        days: Set<LocalDate>,
        today: LocalDate,
    ): LocalDate {
        var cursor = streakStartCursor(
            days,
            today,
        )

        while (cursor in days) {
            cursor = cursor.minus(
                1,
                DateTimeUnit.DAY,
            )
        }

        return cursor
    }
}
