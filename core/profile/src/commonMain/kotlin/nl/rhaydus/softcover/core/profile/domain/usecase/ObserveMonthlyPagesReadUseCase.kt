package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toMonthlyPages
import nl.rhaydus.softcover.core.profile.domain.model.MonthlyPages

class ObserveMonthlyPagesReadUseCase internal constructor(
    private val observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val timeZone: TimeZone,
    private val appDispatchers: AppDispatchers,
) {
    /**
     * Re-emits when a progress update or a read book changes; unfinished books count. The bucketing runs
     * off the main thread.
     */
    operator fun invoke(range: StatsRange): Flow<MonthlyPages> = combine(
        observeProgressUpdatesUseCase(),
        getReadUserBooksUseCase(),
    ) { updates, readBooks ->
        toMonthlyPages(
            updates = updates,
            readBooks = readBooks,
            range = range,
            timeZone = timeZone,
        )
    }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
