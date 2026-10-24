package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats

class ObserveReadingStatsUseCase internal constructor(
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val appDispatchers: AppDispatchers,
) {
    /** Re-emits when the library's read books change; the aggregation runs off the main thread. */
    operator fun invoke(range: StatsRange): Flow<ReadingStats> = getReadUserBooksUseCase()
        .map { books -> books.toReadingStats(range) }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
