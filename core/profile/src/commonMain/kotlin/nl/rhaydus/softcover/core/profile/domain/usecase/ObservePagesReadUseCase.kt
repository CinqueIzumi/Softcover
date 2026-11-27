package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toPagesRead
import nl.rhaydus.softcover.core.profile.domain.model.PagesRead
import kotlin.time.Clock

class ObservePagesReadUseCase internal constructor(
    private val observePageCreditInputsUseCase: ObservePageCreditInputsUseCase,
    private val appDispatchers: AppDispatchers,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    /**
     * Re-emits when a progress update, a Read book or a Did not finish book changes; the aggregation runs
     * off the main thread.
     */
    operator fun invoke(range: StatsRange): Flow<PagesRead> = observePageCreditInputsUseCase()
        .map { inputs ->
            toPagesRead(
                inputs = inputs,
                range = range,
                today = clock.todayIn(timeZone),
                timeZone = timeZone,
            )
        }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
