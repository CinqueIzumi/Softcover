package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toHourlyPages
import nl.rhaydus.softcover.core.profile.domain.model.HourlyPages

class ObserveHourlyPagesReadUseCase internal constructor(
    private val observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase,
    private val timeZone: TimeZone,
    private val appDispatchers: AppDispatchers,
) {
    /** Re-emits when a progress update changes. The bucketing runs off the main thread. */
    operator fun invoke(range: StatsRange): Flow<HourlyPages> = observeProgressUpdatesUseCase()
        .map { updates ->
            toHourlyPages(
                updates = updates,
                range = range,
                timeZone = timeZone,
            )
        }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
