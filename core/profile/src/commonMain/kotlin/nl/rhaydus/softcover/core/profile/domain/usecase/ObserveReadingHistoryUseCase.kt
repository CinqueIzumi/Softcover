package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.profile.domain.mapper.toReadingHistory
import nl.rhaydus.softcover.core.profile.domain.model.ReadingHistory
import kotlin.time.Clock

class ObserveReadingHistoryUseCase internal constructor(
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val appDispatchers: AppDispatchers,
    private val clock: Clock,
    private val timeZone: TimeZone,
) {
    /** Re-emits when the library's read books change; the aggregation runs off the main thread. */
    operator fun invoke(): Flow<ReadingHistory> = getReadUserBooksUseCase()
        .map { books -> books.toReadingHistory(today = clock.todayIn(timeZone)) }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
