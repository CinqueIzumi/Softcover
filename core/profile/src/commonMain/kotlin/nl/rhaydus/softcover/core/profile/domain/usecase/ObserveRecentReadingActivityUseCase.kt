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
    private val timeZone: TimeZone,
) {
    operator fun invoke(): Flow<List<ReadingDayActivity>> =
        profileRepository.observeUserProfileData().map { data ->
            val activeDates = data?.activeReadingDates.orEmpty()
            // "Today" is defined in the device's local timezone so the strip advances at the user's
            // local midnight and a reading-day matches the calendar day they actually read on.
            val today = clock.todayIn(timeZone)
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
