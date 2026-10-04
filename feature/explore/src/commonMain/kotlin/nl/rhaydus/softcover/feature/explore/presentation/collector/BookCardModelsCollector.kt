package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.BECAUSE_YOU_READ_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.SEARCH_RESULT_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.TRENDING_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.UP_NEXT_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toBecauseYouReadCard
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toSearchResultCard
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toTrendingCard
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toUpNextCard
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the trending, because-you-read and up-next rails and the search results to [BookCardUiModel]s off
 * the composition (`component-contract.md` § 7.2 R9), one list per surface: the same book
 * can sit on several surfaces, each stamping its own cover transition key.
 *
 * Derives from the books, [CoverModelsCollector]'s covers and [UnreleasedCoverOverlaysCollector]'s
 * overlays, so it re-runs whenever any of them changes.
 */
internal class BookCardModelsCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map {
                BookCardModelsSnapshot(
                    trendingBooks = it.trendingBooks,
                    becauseYouReadBooks = it.becauseYouReadBooks,
                    continueSeriesBooks = it.continueSeriesBooks,
                    queriedBooks = it.queriedBooks,
                    trendingCovers = it.trendingCovers,
                    becauseYouReadCovers = it.becauseYouReadCovers,
                    continueSeriesCovers = it.continueSeriesCovers,
                    queriedBookCovers = it.queriedBookCovers,
                    unreleasedCoverOverlays = it.unreleasedCoverOverlays,
                    loadingTrendingBooks = it.loadingTrendingBooks,
                    loadingBecauseYouReadBooks = it.loadingBecauseYouReadBooks,
                    loadingContinueSeriesBooks = it.loadingContinueSeriesBooks,
                    isLoading = it.isLoading,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        trendingCards = snapshot.trendingBooks.toCards(
                            loading = snapshot.loadingTrendingBooks,
                            covers = snapshot.trendingCovers,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = TRENDING_STAND_IN_CARDS,
                            map = Book::toTrendingCard,
                        ),
                        becauseYouReadCards = snapshot.becauseYouReadBooks.toCards(
                            loading = snapshot.loadingBecauseYouReadBooks,
                            covers = snapshot.becauseYouReadCovers,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = BECAUSE_YOU_READ_STAND_IN_CARDS,
                            map = Book::toBecauseYouReadCard,
                        ),
                        upNextCards = snapshot.continueSeriesBooks.toCards(
                            loading = snapshot.loadingContinueSeriesBooks,
                            covers = snapshot.continueSeriesCovers,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = UP_NEXT_STAND_IN_CARDS,
                            map = Book::toUpNextCard,
                        ),
                        queriedBookCards = snapshot.queriedBooks
                            .takeUnless { snapshot.isLoading || snapshot.firstResultLacksCover }
                            ?.mapNotNull { book ->
                                snapshot.queriedBookCovers[book.id]?.let { cover ->
                                    book.toSearchResultCard(cover = cover)
                                }
                            }
                            ?.toImmutableList()
                            ?: SEARCH_RESULT_STAND_IN_CARDS,
                    )
                }
            }
    }
}

// Only the first row is checked so a load-more page does not swap the list back to stand-ins while its
// covers catch up.
private val BookCardModelsSnapshot.firstResultLacksCover: Boolean
    get() = queriedBooks.firstOrNull()?.let { it.id !in queriedBookCovers } == true

private inline fun List<Book>.toCards(
    loading: Boolean,
    covers: Map<Int, CoverUiModel>,
    overlays: Map<Int, CoverOverlayUiModel>,
    standIns: ImmutableList<BookCardUiModel>,
    map: (Book, CoverUiModel?, CoverOverlayUiModel?) -> BookCardUiModel,
): ImmutableList<BookCardUiModel> = if (loading && isEmpty()) {
    standIns
} else {
    map { book ->
        map(
            book,
            covers[book.id],
            overlays[book.id],
        )
    }.toImmutableList()
}
