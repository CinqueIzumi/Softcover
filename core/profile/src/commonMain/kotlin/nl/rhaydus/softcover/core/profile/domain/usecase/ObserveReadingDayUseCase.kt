package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.profile.domain.mapper.toReadingDay
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDay

class ObserveReadingDayUseCase internal constructor(
    private val observePageCreditInputsUseCase: ObservePageCreditInputsUseCase,
    private val timeZone: TimeZone,
    private val appDispatchers: AppDispatchers,
) {
    /**
     * Re-emits when a progress update, a Read book or a Did not finish book changes; unfinished books
     * count. The bucketing runs off the main thread.
     */
    operator fun invoke(date: LocalDate): Flow<ReadingDay> = observePageCreditInputsUseCase()
        .map { inputs ->
            toReadingDay(
                updates = inputs.updates,
                readBooks = inputs.readBooks,
                didNotFinishBooks = inputs.didNotFinishBooks,
                date = date,
                timeZone = timeZone,
            )
        }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
