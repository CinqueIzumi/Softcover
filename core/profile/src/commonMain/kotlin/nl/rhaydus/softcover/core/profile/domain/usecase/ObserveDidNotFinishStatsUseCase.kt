package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUserTagsByBookUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllDidNotFinishReasonsUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toDidNotFinishStats
import nl.rhaydus.softcover.core.profile.domain.model.DidNotFinishStats

class ObserveDidNotFinishStatsUseCase internal constructor(
    private val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    private val observeUserTagsByBookUseCase: ObserveUserTagsByBookUseCase,
    private val observeAllDidNotFinishReasonsUseCase: ObserveAllDidNotFinishReasonsUseCase,
    private val appDispatchers: AppDispatchers,
) {
    operator fun invoke(range: StatsRange): Flow<DidNotFinishStats> = combine(
        getDidNotFinishUserBooksUseCase(),
        observeUserTagsByBookUseCase(),
        observeAllDidNotFinishReasonsUseCase(),
    ) { books, userTagsByBookId, reasonsByBookId ->
        books.toDidNotFinishStats(
            range = range,
            userTagsByBookId = userTagsByBookId,
            reasonsByBookId = reasonsByBookId,
        )
    }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
