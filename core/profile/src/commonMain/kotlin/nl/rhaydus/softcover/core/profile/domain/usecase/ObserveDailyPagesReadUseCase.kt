package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.profile.domain.mapper.toDailyPages
import nl.rhaydus.softcover.core.profile.domain.model.DailyPages

class ObserveDailyPagesReadUseCase internal constructor(
    private val observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val timeZone: TimeZone,
    private val appDispatchers: AppDispatchers,
) {
    /**
     * Re-emits when a progress update or a read book changes; unfinished books count. The bucketing runs
     * off the main thread.
     */
    operator fun invoke(year: Int): Flow<DailyPages> = combine(
        observeProgressUpdatesUseCase(),
        getReadUserBooksUseCase(),
    ) { updates, readBooks ->
        toDailyPages(
            updates = updates,
            readBooks = readBooks,
            year = year,
            timeZone = timeZone,
        )
    }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
