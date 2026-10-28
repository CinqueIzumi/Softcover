package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUserTagsByBookUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toReadingStats
import nl.rhaydus.softcover.core.profile.domain.model.ReadingStats

class ObserveReadingStatsUseCase internal constructor(
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val observeUserTagsByBookUseCase: ObserveUserTagsByBookUseCase,
    private val appDispatchers: AppDispatchers,
) {
    /** Re-emits when the read books or the user's tags change; the aggregation runs off the main thread. */
    operator fun invoke(range: StatsRange): Flow<ReadingStats> = combine(
        getReadUserBooksUseCase(),
        observeUserTagsByBookUseCase(),
    ) { books, userTagsByBookId ->
        books.toReadingStats(
            range = range,
            userTagsByBookId = userTagsByBookId,
        )
    }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
