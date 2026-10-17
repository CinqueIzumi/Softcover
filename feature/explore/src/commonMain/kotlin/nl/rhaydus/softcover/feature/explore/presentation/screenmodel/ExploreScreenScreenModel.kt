package nl.rhaydus.softcover.feature.explore.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.book.domain.usecase.GetAllUserBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.MarkBookAsWantToReadUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingFetchedAtUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshTrendingBooksUseCase
import nl.rhaydus.softcover.core.component.state.OfflineCopy
import nl.rhaydus.softcover.core.preferences.domain.usecase.SetBecauseYouReadGenreUseCase
import nl.rhaydus.softcover.core.presentation.removal.UndoableBookRemover
import nl.rhaydus.softcover.feature.explore.domain.usecase.ClearSearchResultsUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesBookUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.DismissContinueSeriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.EnrichDismissedContinueSeriesMetadataUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetBecauseYouReadBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetContinueSeriesBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetFeaturedUpcomingReleaseUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetMoodTagsUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetPreviousSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetQueriedBooksHasMoreUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.GetQueriedBooksUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.RemoveAllSearchQueriesUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.RemoveSearchQueryUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchByMoodUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.SearchForNameUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesBookDismissalUseCase
import nl.rhaydus.softcover.feature.explore.domain.usecase.UndoContinueSeriesDismissalUseCase
import nl.rhaydus.softcover.feature.explore.presentation.action.ExploreAction
import nl.rhaydus.softcover.feature.explore.presentation.collector.ExploreCollector
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ToadScreenModel

internal class ExploreScreenScreenModel(
    private val getPreviousSearchQueriesUseCase: GetPreviousSearchQueriesUseCase,
    private val getQueriedBooksUseCase: GetQueriedBooksUseCase,
    private val getQueriedBooksHasMoreUseCase: GetQueriedBooksHasMoreUseCase,
    private val searchForNameUseCase: SearchForNameUseCase,
    private val clearSearchResultsUseCase: ClearSearchResultsUseCase,
    private val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    private val removeSearchQueryUseCase: RemoveSearchQueryUseCase,
    private val removeAllSearchQueriesUseCase: RemoveAllSearchQueriesUseCase,
    private val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    private val undoableBookRemover: UndoableBookRemover,
    private val observeTrendingBooksUseCase: ObserveTrendingBooksUseCase,
    private val refreshTrendingBooksUseCase: RefreshTrendingBooksUseCase,
    private val observeTrendingFetchedAtUseCase: ObserveTrendingFetchedAtUseCase,
    private val getContinueSeriesBooksUseCase: GetContinueSeriesBooksUseCase,
    private val dismissContinueSeriesBookUseCase: DismissContinueSeriesBookUseCase,
    private val dismissContinueSeriesUseCase: DismissContinueSeriesUseCase,
    private val undoContinueSeriesBookDismissalUseCase: UndoContinueSeriesBookDismissalUseCase,
    private val undoContinueSeriesDismissalUseCase: UndoContinueSeriesDismissalUseCase,
    private val enrichDismissedContinueSeriesMetadataUseCase: EnrichDismissedContinueSeriesMetadataUseCase,
    private val getFeaturedUpcomingReleaseUseCase: GetFeaturedUpcomingReleaseUseCase,
    private val getBecauseYouReadBooksUseCase: GetBecauseYouReadBooksUseCase,
    private val getMoodTagsUseCase: GetMoodTagsUseCase,
    private val searchByMoodUseCase: SearchByMoodUseCase,
    private val setBecauseYouReadGenreUseCase: SetBecauseYouReadGenreUseCase,
    offlineCopy: OfflineCopy,
    flows: List<ExploreCollector>,
    appDispatchers: AppDispatchers,
) : ToadScreenModel<ExploreScreenUiState, ExploreEvent, ExploreDependencies, ExploreCollector, ExploreLocalVariables>(
    initialState = ExploreScreenUiState(),
    initialLocalVariables = ExploreLocalVariables(),
    initializers = flows,
) {
    override val dependencies: ExploreDependencies = ExploreDependencies(
        searchForNameUseCase = searchForNameUseCase,
        clearSearchResultsUseCase = clearSearchResultsUseCase,
        getQueriedBooksUseCase = getQueriedBooksUseCase,
        getQueriedBooksHasMoreUseCase = getQueriedBooksHasMoreUseCase,
        coroutineScope = screenModelScope,
        mainDispatcher = appDispatchers.main,
        getPreviousSearchQueriesUseCase = getPreviousSearchQueriesUseCase,
        removeSearchQueryUseCase = removeSearchQueryUseCase,
        getAllUserBooksUseCase = getAllUserBooksUseCase,
        removeAllSearchQueriesUseCase = removeAllSearchQueriesUseCase,
        markBookAsWantToReadUseCase = markBookAsWantToReadUseCase,
        undoableBookRemover = undoableBookRemover,
        observeTrendingBooksUseCase = observeTrendingBooksUseCase,
        refreshTrendingBooksUseCase = refreshTrendingBooksUseCase,
        observeTrendingFetchedAtUseCase = observeTrendingFetchedAtUseCase,
        getContinueSeriesBooksUseCase = getContinueSeriesBooksUseCase,
        dismissContinueSeriesBookUseCase = dismissContinueSeriesBookUseCase,
        dismissContinueSeriesUseCase = dismissContinueSeriesUseCase,
        undoContinueSeriesBookDismissalUseCase = undoContinueSeriesBookDismissalUseCase,
        undoContinueSeriesDismissalUseCase = undoContinueSeriesDismissalUseCase,
        enrichDismissedContinueSeriesMetadataUseCase = enrichDismissedContinueSeriesMetadataUseCase,
        getFeaturedUpcomingReleaseUseCase = getFeaturedUpcomingReleaseUseCase,
        getBecauseYouReadBooksUseCase = getBecauseYouReadBooksUseCase,
        getMoodTagsUseCase = getMoodTagsUseCase,
        searchByMoodUseCase = searchByMoodUseCase,
        setBecauseYouReadGenreUseCase = setBecauseYouReadGenreUseCase,
        offlineCopy = offlineCopy,
    )

    init {
        startInitializers()
    }

    fun runAction(action: ExploreAction) = dispatch(action = action)
}
