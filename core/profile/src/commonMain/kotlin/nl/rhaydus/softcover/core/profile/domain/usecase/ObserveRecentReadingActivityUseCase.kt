package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

/** Number of days surfaced by the Reading-screen streak strip (and persisted for it). */
internal const val READING_ACTIVITY_WINDOW_DAYS = 21

class ObserveRecentReadingActivityUseCase(
    private val profileRepository: ProfileRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<List<ReadingDayActivity>> =
        profileRepository.observeUserProfileData().map { data ->
            val activeDates = data?.activeReadingDates.orEmpty()
            // "Today" is defined in UTC to match the server's UTC calendar dates for reading
            // activity; the strip therefore advances at UTC midnight, not the device's local midnight.
            val today = clock.todayIn(TimeZone.UTC)
            val firstDay = today.minus(
                READING_ACTIVITY_WINDOW_DAYS - 1,
                DateTimeUnit.DAY,
            )

            (0 until READING_ACTIVITY_WINDOW_DAYS).map { offset ->
                val date = firstDay.plus(
                    offset,
                    DateTimeUnit.DAY,
                )

                ReadingDayActivity(
                    date = date,
                    didRead = date in activeDates,
                )
            }
        }
}
