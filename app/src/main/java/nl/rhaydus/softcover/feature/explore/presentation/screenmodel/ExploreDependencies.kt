package nl.rhaydus.softcover.feature.explore.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import nl.rhaydus.softcover.core.presentation.toad.ActionDependencies
import nl.rhaydus.softcover.feature.books.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.feature.books.domain.usecase.RemoveBookFromLibraryUseCase
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
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
