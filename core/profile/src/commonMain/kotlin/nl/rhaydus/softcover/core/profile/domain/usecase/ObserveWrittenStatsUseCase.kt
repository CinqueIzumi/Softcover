package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.personal.domain.usecase.ObserveAllQuotesUseCase
import nl.rhaydus.softcover.core.profile.domain.mapper.toWrittenStats
import nl.rhaydus.softcover.core.profile.domain.model.WrittenStats

class ObserveWrittenStatsUseCase internal constructor(
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val observeAllQuotesUseCase: ObserveAllQuotesUseCase,
    private val timeZone: TimeZone,
    private val appDispatchers: AppDispatchers,
) {
    operator fun invoke(range: StatsRange): Flow<WrittenStats> = combine(
        getAllUserBooksUseCase(),
        observeAllQuotesUseCase(),
    ) { books, quotes ->
        toWrittenStats(
            books = books,
            quotes = quotes,
            range = range,
            timeZone = timeZone,
        )
    }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
