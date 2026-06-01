package nl.rhaydus.softcover.feature.explore.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RemoveBookFromLibraryUseCase
import nl.rhaydus.softcover.core.domain.model.AppDispatchers
import nl.rhaydus.softcover.core.presentation.toad.ToadScreenModel
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
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.flows.ExploreInitializer
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState

class ExploreScreenScreenModel(
    private val getPreviousSearchQueriesUseCase: GetPreviousSearchQueriesUseCase,
    private val getQueriedBooksUseCase: GetQueriedBooksUseCase,
    private val searchForNameUseCase: SearchForNameUseCase,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val removeSearchQueryUseCase: RemoveSearchQueryUseCase,
    private val removeAllSearchQueriesUseCase: RemoveAllSearchQueriesUseCase,
    private val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    private val removeBookFromLibraryUseCase: RemoveBookFromLibraryUseCase,
    private val getTrendingBooksUseCase: GetTrendingBooksUseCase,
    private val getContinueSeriesBooksUseCase: GetContinueSeriesBooksUseCase,
    private val dismissContinueSeriesBookUseCase: DismissContinueSeriesBookUseCase,
    private val dismissContinueSeriesUseCase: DismissContinueSeriesUseCase,
    private val undoContinueSeriesBookDismissalUseCase: UndoContinueSeriesBookDismissalUseCase,
    private val undoContinueSeriesDismissalUseCase: UndoContinueSeriesDismissalUseCase,
    flows: List<ExploreInitializer>,
    appDispatchers: AppDispatchers,
) : ToadScreenModel<ExploreScreenUiState, ExploreEvent, ExploreDependencies, ExploreInitializer, ExploreLocalVariables>(
    initialState = ExploreScreenUiState(),
    initialLocalVariables = ExploreLocalVariables(),
    initializers = flows,
) {
    override val dependencies: ExploreDependencies = ExploreDependencies(
        searchForNameUseCase = searchForNameUseCase,
        getQueriedBooksUseCase = getQueriedBooksUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        getPreviousSearchQueriesUseCase = getPreviousSearchQueriesUseCase,
        removeSearchQueryUseCase = removeSearchQueryUseCase,
        getAllUserBooksUseCase = getAllUserBooksUseCase,
        removeAllSearchQueriesUseCase = removeAllSearchQueriesUseCase,
        markBookAsWantToReadUseCase = markBookAsWantToReadUseCase,
        removeBookFromLibraryUseCase = removeBookFromLibraryUseCase,
        getTrendingBooksUseCase = getTrendingBooksUseCase,
        getContinueSeriesBooksUseCase = getContinueSeriesBooksUseCase,
        dismissContinueSeriesBookUseCase = dismissContinueSeriesBookUseCase,
        dismissContinueSeriesUseCase = dismissContinueSeriesUseCase,
        undoContinueSeriesBookDismissalUseCase = undoContinueSeriesBookDismissalUseCase,
        undoContinueSeriesDismissalUseCase = undoContinueSeriesDismissalUseCase,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ExploreAction) = dispatch(action = action)
}
