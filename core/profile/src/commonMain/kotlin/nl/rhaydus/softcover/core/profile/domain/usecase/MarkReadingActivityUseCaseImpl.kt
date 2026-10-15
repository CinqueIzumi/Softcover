package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.todayIn
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.activity.MarkReadingActivityUseCase
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Resolves `actionAt` to a calendar day in the injected [timeZone], the zone
 * [ObserveRecentReadingActivityUseCase] builds its window in, so the marked day lines up with the strip.
 */
internal class MarkReadingActivityUseCaseImpl(
    private val profileRepository: ProfileRepository,
    private val clock: Clock,
    private val timeZone: TimeZone,
) : MarkReadingActivityUseCase {
    override suspend operator fun invoke(actionAt: String?) {
        // Best-effort optimistic write riding on a book mutation: it must never fail the mutation that
        // triggered it (the Result is dropped), but a coroutine cancellation still propagates and any
        // other failure is logged — both guaranteed by runCatchingLogged.
        runCatchingLogged(context = "Failed to mark active reading date for $actionAt") {
            val today = clock.todayIn(timeZone)
            val date = minOf(
                actionAt?.let { Instant.parse(it).toLocalDateTime(timeZone).date } ?: today,
                today,
            )

            profileRepository.markActiveReadingDate(date = date)
        }
    }
}
