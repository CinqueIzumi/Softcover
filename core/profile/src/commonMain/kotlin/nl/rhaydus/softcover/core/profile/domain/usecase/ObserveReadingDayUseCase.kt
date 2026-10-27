package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.profile.domain.mapper.toReadingDay
import nl.rhaydus.softcover.core.profile.domain.model.ReadingDay

class ObserveReadingDayUseCase internal constructor(
    private val observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val timeZone: TimeZone,
    private val appDispatchers: AppDispatchers,
) {
    /**
     * Re-emits when a progress update or a read book changes; unfinished books count. The bucketing runs
     * off the main thread.
     */
    operator fun invoke(date: LocalDate): Flow<ReadingDay> = combine(
        observeProgressUpdatesUseCase(),
        getReadUserBooksUseCase(),
    ) { updates, readBooks ->
        toReadingDay(
            updates = updates,
            readBooks = readBooks,
            date = date,
            timeZone = timeZone,
        )
    }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
