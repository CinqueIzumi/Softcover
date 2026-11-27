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
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookMoodsUseCase
import nl.rhaydus.softcover.core.corpus.domain.usecase.ObserveAllBookTraitsUseCase
import nl.rhaydus.softcover.core.domain.model.StatsRange
import nl.rhaydus.softcover.core.profile.domain.mapper.toHowYouReadStats
import nl.rhaydus.softcover.core.profile.domain.model.HowYouReadStats

class ObserveHowYouReadStatsUseCase internal constructor(
    private val getCurrentlyReadingUserBooksUseCase: GetCurrentlyReadingUserBooksUseCase,
    private val getPausedUserBooksUseCase: GetPausedUserBooksUseCase,
    private val getReadUserBooksUseCase: GetReadUserBooksUseCase,
    private val getDidNotFinishUserBooksUseCase: GetDidNotFinishUserBooksUseCase,
    private val observeProgressUpdatesUseCase: ObserveProgressUpdatesUseCase,
    private val observeAllBookTraitsUseCase: ObserveAllBookTraitsUseCase,
    private val observeAllBookMoodsUseCase: ObserveAllBookMoodsUseCase,
    private val appDispatchers: AppDispatchers,
    private val timeZone: TimeZone,
) {
    operator fun invoke(range: StatsRange): Flow<HowYouReadStats> = combine(
        observeBooksWithProgress(
            getCurrentlyReadingUserBooksUseCase = getCurrentlyReadingUserBooksUseCase,
            getPausedUserBooksUseCase = getPausedUserBooksUseCase,
            getReadUserBooksUseCase = getReadUserBooksUseCase,
            getDidNotFinishUserBooksUseCase = getDidNotFinishUserBooksUseCase,
            observeProgressUpdatesUseCase = observeProgressUpdatesUseCase,
        ),
        observeAllBookTraitsUseCase(),
        observeAllBookMoodsUseCase(),
    ) { (books, updates), traitsByBookId, moodsByBookId ->
        books.toHowYouReadStats(
            range = range,
            updates = updates,
            traitsByBookId = traitsByBookId,
            moodsByBookId = moodsByBookId,
            timeZone = timeZone,
        )
    }
        .flowOn(appDispatchers.default)
        .distinctUntilChanged()
}
