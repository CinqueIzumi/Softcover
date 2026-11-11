package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toMonthlyPages
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages

class ObserveMonthlyPagesReadUseCase internal constructor(
    private val observePageCreditInputsUseCase: ObservePageCreditInputsUseCase,
    private val timeZone: TimeZone,
    private val appDispatchers: AppDispatchers,
) {
    /**
     * Re-emits when a progress update, a Read book or a Did not finish book changes; unfinished books
     * count. The bucketing runs off the main thread.
     */
    operator fun invoke(range: StatsRange): Flow<MonthlyPages> = observePageCreditInputsUseCase()
        .map { inputs ->
            toMonthlyPages(
                updates = inputs.updates,
                readBooks = inputs.readBooks,
                didNotFinishBooks = inputs.didNotFinishBooks,
                range = range,
                timeZone = timeZone,
            )
        }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
