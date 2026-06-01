package nl.rhaydus.softcover.feature.explore.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesBookUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetTrendingBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesBookDismissalUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesDismissalUseCase

data class ExploreDependencies(
    val getPreviousSearchQueriesUseCase: GetPreviousSearchQueriesUseCase,
    val getQueriedBooksUseCase: GetQueriedBooksUseCase,
    val searchForNameUseCase: SearchForNameUseCase,
    val removeSearchQueryUseCase: RemoveSearchQueryUseCase,
    val removeAllSearchQueriesUseCase: RemoveAllSearchQueriesUseCase,
    val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    val removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase,
    val getTrendingBooksUseCase: GetTrendingBooksUseCase,
    val getContinueSeriesBooksUseCase: GetContinueSeriesBooksUseCase,
    val dismissContinueSeriesBookUseCase: DismissContinueSeriesBookUseCase,
    val dismissContinueSeriesUseCase: DismissContinueSeriesUseCase,
    val undoContinueSeriesBookDismissalUseCase: UndoContinueSeriesBookDismissalUseCase,
    val undoContinueSeriesDismissalUseCase: UndoContinueSeriesDismissalUseCase,
    // Bumping this re-subscribes the continue-series flow in its collector, forcing
    // the `fetchNextInSeries` network calls to run again on user-triggered refresh.
    val continueSeriesRefreshTrigger: MutableStateFlow<Long> = MutableStateFlow(0L),
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
