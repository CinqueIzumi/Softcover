package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.repository.RandomPickRepository
import nl.rhaydus.softcover.feature.widget.domain.util.weeklyPickExpiry
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * The earliest Weekly pick expiry still ahead. A past expiry is skipped: the maintenance run that fires at it
 * reschedules before its repaint has re-picked, and would otherwise schedule itself again at once.
 */
internal class GetNextWeeklyPickExpiryUseCase(
    private val randomPickRepository: RandomPickRepository,
    private val clock: Clock = Clock.System,
) {
    suspend operator fun invoke(appWidgetIds: List<Int>): Result<Instant?> = runCatchingCancellable {
        val now = clock.now()

        appWidgetIds
            .mapNotNull { appWidgetId ->
                weeklyPickExpiry(
                    settings = randomPickRepository.getSettings(appWidgetId = appWidgetId),
                    state = randomPickRepository.getState(appWidgetId = appWidgetId),
                )
            }
            .filter { it > now }
            .minOrNull()
    }
}
