package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
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
 * the composition (`component-contract.md` § 7.2 R9), keyed by [Book.id] into one map per surface: the same
 * book can sit on several surfaces, each stamping its own cover transition key.
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
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        trendingCards = snapshot.trendingBooks.toCards(
                            covers = snapshot.trendingCovers,
                            overlays = snapshot.unreleasedCoverOverlays,
                            map = Book::toTrendingCard,
                        ),
                        becauseYouReadCards = snapshot.becauseYouReadBooks.toCards(
                            covers = snapshot.becauseYouReadCovers,
                            overlays = snapshot.unreleasedCoverOverlays,
                            map = Book::toBecauseYouReadCard,
                        ),
                        upNextCards = snapshot.continueSeriesBooks.toCards(
                            covers = snapshot.continueSeriesCovers,
                            overlays = snapshot.unreleasedCoverOverlays,
                            map = Book::toUpNextCard,
                        ),
                        queriedBookCards = snapshot.queriedBooks.mapNotNull { book ->
                            snapshot.queriedBookCovers[book.id]?.let { cover ->
                                book.id to book.toSearchResultCard(cover = cover)
                            }
                        }.toMap().toImmutableMap(),
                    )
                }
            }
    }
}

private inline fun List<Book>.toCards(
    covers: Map<Int, CoverUiModel>,
    overlays: Map<Int, CoverOverlayUiModel>,
    map: (Book, CoverUiModel?, CoverOverlayUiModel?) -> BookCardUiModel,
): ImmutableMap<Int, BookCardUiModel> = associate { book ->
    book.id to map(
        book,
        covers[book.id],
        overlays[book.id],
    )
}.toImmutableMap()
