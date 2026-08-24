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
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

class RefreshReadingActivityUseCase(
    private val profileRepository: ProfileRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
    private val activityRefreshGate: ProfileRefreshGate,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    // Skips the network fetch once this session has already refreshed successfully - every other
    // caller (startup, the Reading screen, the Profile tab's composed refresh) renders from the
    // DataStore cache. runOnce guards check-fetch-mark atomically and marks only after a
    // successful cache write, so a transient failure leaves the gate open and the next call
    // retries. Gated independently from the stats half (RefreshUserProfileStatsUseCase), so
    // deferring stats off startup never delays this.
    suspend operator fun invoke(): Result<Unit> = runCatchingLogged {
        activityRefreshGate.runOnce {
            val userId = getUserIdUseCase().getOrThrow()
            val today = clock.todayIn(timeZone)
            val windowStart = today.minus(
                READING_ACTIVITY_WINDOW_DAYS - 1,
                DateTimeUnit.DAY,
            )
            val (streakDays, recentReadingDays) = collectProfileDays(
                userId = userId,
                today = today,
                windowStart = windowStart,
            )
            val readingStreak = computeStreak(
                days = streakDays,
                today = today,
            )

            profileRepository.cacheUserProfileActivity(
                readingStreak = readingStreak,
                recentReadingDays = recentReadingDays,
            )
        }
    }

    // Walks the descending reading-day stream newest-to-oldest once, collecting whatever either
    // consumer still needs from it: the streak walk (until a gap is fully determined) and the
    // fixed-size recent window (until a date falls before windowStart). Each was previously its
    // own independent paging run over the same rows - collapsing them here means the account's
    // reading history is paged at most once per refresh instead of twice. transformWhile keeps
    // the early-cancellation property of the original streak walk: once both conditions are
    // false, the upstream paging in ProfileRemoteDataSource is cancelled rather than exhausted.
    private suspend fun collectProfileDays(
        userId: Int,
        today: LocalDate,
        windowStart: LocalDate,
    ): Pair<Set<LocalDate>, Set<LocalDate>> {
        val streakDays = mutableSetOf<LocalDate>()
        val recentReadingDays = mutableSetOf<LocalDate>()

        profileRepository.streamReadingDaysDescending(userId = userId)
            .transformWhile { date ->
                streakDays += date

                if (date >= windowStart) recentReadingDays += date

                emit(date)

                hasUndeterminedStreakGap(
                    days = streakDays,
                    today = today,
                ) || date >= windowStart
            }
            .collect()

        return streakDays to recentReadingDays
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
