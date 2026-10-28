package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.countExcludedReads
import nl.rhaydus.softcover.core.profile.domain.mapper.toReadDurations
import nl.rhaydus.softcover.core.profile.domain.mapper.toTimeToFinish
import nl.rhaydus.softcover.core.profile.domain.model.TimeToFinish

class ObserveTimeToFinishUseCase internal constructor(
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val appDispatchers: AppDispatchers,
) {
    operator fun invoke(range: StatsRange): Flow<TimeToFinish> = getAllUserBooksUseCase()
        .map { books ->
            books.toReadDurations(range)
                .toTimeToFinish(excludedReadCount = books.countExcludedReads(range))
        }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
