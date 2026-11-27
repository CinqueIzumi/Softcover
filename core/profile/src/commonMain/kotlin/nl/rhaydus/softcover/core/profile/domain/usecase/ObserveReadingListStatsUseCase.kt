package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toReadingListStats
import nl.rhaydus.softcover.core.profile.domain.model.ReadingListStats

class ObserveReadingListStatsUseCase internal constructor(
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val appDispatchers: AppDispatchers,
) {
    /** Re-emits when the library changes; the aggregation runs off the main thread. */
    operator fun invoke(range: StatsRange): Flow<ReadingListStats> = getAllUserBooksUseCase()
        .map { books -> books.toReadingListStats(range = range) }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
