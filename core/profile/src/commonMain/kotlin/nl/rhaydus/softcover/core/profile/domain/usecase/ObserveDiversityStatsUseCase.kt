package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.TimeZone
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetCurrentlyReadingUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetDidNotFinishUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetPausedUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.GetReadUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveProgressUpdatesUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookCopiesUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookTraitsUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toDiversityStats
import nl.rhaydus.softcover.core.profile.domain.model.DiversityStats

class ObserveDiversityStatsUseCase internal constructor(
    private val getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    private val getPausedUserBooksUseCase: GetPausedUserBooksUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    private val observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase,
    private val observeAllBookTraitsUseCase: ObserveAllBookTraitsUseCase,
    private val observeAllBookCopiesUseCase: ObserveAllBookCopiesUseCase,
    private val appDispatchers: AppDispatchers,
    private val timeZone: TimeZone,
) {
    operator fun invoke(range: StatsRange): Flow<DiversityStats> = combine(
        observeBooksWithProgress(
            getCurrentlyReadingUserBooksUseCase = getCurrentlyReadingUserBooksUseCase,
            getPausedUserBooksUseCase = getPausedUserBooksUseCase,
            getReadUserBooksUseCase = getReadUserBooksUseCase,
            getDidNotFinishUserBooksUseCase = getDidNotFinishUserBooksUseCase,
            observeProgressUpdatesUseCase = observeProgressUpdatesUseCase,
        ),
        observeAllBookTraitsUseCase(),
        observeAllBookCopiesUseCase(),
    ) { (books, updates), traitsByBookId, copiesByBookId ->
        books.toDiversityStats(
            range = range,
            updates = updates,
            traitsByBookId = traitsByBookId,
            copiesByBookId = copiesByBookId,
            timeZone = timeZone,
        )
    }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
