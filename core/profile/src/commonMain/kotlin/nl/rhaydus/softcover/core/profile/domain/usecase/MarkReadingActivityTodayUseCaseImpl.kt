package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlin.coroutines.cancellation.CancellationException
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityTodayUseCase
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

/**
 * Marks today as an active reading day in the profile cache, using the device's local timezone so the
 * day matches the user's calendar and the window computed by [ObserveRecentReadingActivityUseCase] — the
 * optimistically lit dot then lines up with the strip.
 */
internal class MarkReadingActivityTodayUseCaseImpl(
    private val profileRepository: ProfileRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : MarkReadingActivityTodayUseCase {
    override suspend operator fun invoke() {
        val today = clock.todayIn(timeZone)

        // This is a best-effort optimistic write riding on a book mutation — it must never fail the
        // mutation that triggered it, but a coroutine cancellation still has to propagate.
        runCatching {
            profileRepository.markActiveReadingDate(date = today)
        }.onFailure { error ->
            if (error is CancellationException) throw error

            AppLog.e(error)
        }
    }
}
