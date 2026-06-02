package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository
import java.time.Clock
import java.time.LocalDate

/** Number of days surfaced by the Reading-screen streak strip (and persisted for it). */
internal const val READING_ACTIVITY_WINDOW_DAYS = 21

class ObserveRecentReadingActivityUseCase(
    private val profileRepository: ProfileRepository,
    private val clock: Clock,
) {
    operator fun invoke(): Flow<List<ReadingDayActivity>> =
        profileRepository.observeUserProfileData().map { data ->
            val activeDates = data?.activeReadingDates.orEmpty()
            val today = LocalDate.now(clock)
            val firstDay = today.minusDays((READING_ACTIVITY_WINDOW_DAYS - 1).toLong())

            (0 until READING_ACTIVITY_WINDOW_DAYS).map { offset ->
                val date = firstDay.plusDays(offset.toLong())

                ReadingDayActivity(
                    date = date,
                    didRead = date in activeDates,
                )
            }
        }
}
