package nl.rhaydus.softcover.feature.explore.presentation.screenmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
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
import nl.rhaydus.toad.ActionDependencies

internal data class ExploreDependencies(
    val getPreviousSearchQueriesUseCase: GetPreviousSearchQueriesUseCase,
    val getQueriedBooksUseCase: GetQueriedBooksUseCase,
    val getQueriedBooksHasMoreUseCase: GetQueriedBooksHasMoreUseCase,
    val searchForNameUseCase: SearchForNameUseCase,
    val clearSearchResultsUseCase: ClearSearchResultsUseCase,
    val removeSearchQueryUseCase: RemoveSearchQueryUseCase,
    val removeAllSearchQueriesUseCase: RemoveAllSearchQueriesUseCase,
    val getAllUserBooksUseCase: GetAllUserBooksUseCase,
    val markBookAsWantToReadUseCase: MarkBookAsWantToReadUseCase,
    val undoableBookRemover: UndoableBookRemover,
    val observeTrendingBooksUseCase: ObserveTrendingBooksUseCase,
    val refreshTrendingBooksUseCase: RefreshTrendingBooksUseCase,
    val observeTrendingFetchedAtUseCase: ObserveTrendingFetchedAtUseCase,
    val getContinueSeriesBooksUseCase: GetContinueSeriesBooksUseCase,
    val dismissContinueSeriesBookUseCase: DismissContinueSeriesBookUseCase,
    val dismissContinueSeriesUseCase: DismissContinueSeriesUseCase,
    val undoContinueSeriesBookDismissalUseCase: UndoContinueSeriesBookDismissalUseCase,
    val undoContinueSeriesDismissalUseCase: UndoContinueSeriesDismissalUseCase,
    val enrichDismissedContinueSeriesMetadataUseCase: EnrichDismissedContinueSeriesMetadataUseCase,
    val getFeaturedUpcomingReleaseUseCase: GetFeaturedUpcomingReleaseUseCase,
    val getBecauseYouReadBooksUseCase: GetBecauseYouReadBooksUseCase,
    val getMoodTagsUseCase: GetMoodTagsUseCase,
    val searchByMoodUseCase: SearchByMoodUseCase,
    val setBecauseYouReadGenreUseCase: SetBecauseYouReadGenreUseCase,
    val offlineCopy: OfflineCopy,
    // Bumping this re-subscribes the continue-series flow in its collector, forcing
    // the `fetchNextInSeries` network calls to run again on user-triggered refresh.
    val continueSeriesRefreshTrigger: MutableStateFlow<Long> = MutableStateFlow(0L),
    override val coroutineScope: CoroutineScope,
    override val mainDispatcher: CoroutineDispatcher,
) : ActionDependencies()
