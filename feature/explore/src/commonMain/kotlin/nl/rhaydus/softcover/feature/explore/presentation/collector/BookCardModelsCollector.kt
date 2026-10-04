package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
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
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_TRENDING
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the trending, because-you-read and up-next rails and the search results to [BookCardUiModel]s off
 * the composition (`component-contract.md` § 7.2 R9), one list per surface: the same book
 * can sit on several surfaces, each stamping its own cover transition key.
 *
 * Derives from the books and [UnreleasedCoverOverlaysCollector]'s overlays, so it re-runs whenever
 * either changes.
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
                            coverVariant = CoverVariant.Raised,
                            coverSurface = SURFACE_TRENDING,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = TRENDING_STAND_IN_CARDS,
                            map = Book::toTrendingCard,
                        ),
                        becauseYouReadCards = snapshot.becauseYouReadBooks.toCards(
                            loading = snapshot.loadingBecauseYouReadBooks,
                            coverVariant = CoverVariant.Raised,
                            coverSurface = SURFACE_BECAUSE_YOU_READ,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = BECAUSE_YOU_READ_STAND_IN_CARDS,
                            map = Book::toBecauseYouReadCard,
                        ),
                        upNextCards = snapshot.continueSeriesBooks.toCards(
                            loading = snapshot.loadingContinueSeriesBooks,
                            coverVariant = CoverVariant.Lifted,
                            coverSurface = SURFACE_UP_NEXT,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = UP_NEXT_STAND_IN_CARDS,
                            map = Book::toUpNextCard,
                        ),
                        queriedBookCards = snapshot.queriedBooks
                            .takeUnless { snapshot.isLoading }
                            ?.map { book ->
                                book.toSearchResultCard(
                                    cover = book.toCoverModel(
                                        variant = CoverVariant.Flat,
                                        surface = null,
                                    ),
                                )
                            }
                            ?.toImmutableList()
                            ?: SEARCH_RESULT_STAND_IN_CARDS,
                    )
                }
            }
    }
}

private inline fun List<Book>.toCards(
    loading: Boolean,
    coverVariant: CoverVariant,
    coverSurface: String,
    overlays: Map<Int, CoverOverlayUiModel>,
    standIns: ImmutableList<BookCardUiModel>,
    map: (Book, CoverUiModel?, CoverOverlayUiModel?) -> BookCardUiModel,
): ImmutableList<BookCardUiModel> = if (loading && isEmpty()) {
    standIns
} else {
    map { book ->
        map(
            book,
            book.toCoverModel(
                variant = coverVariant,
                surface = coverSurface,
            ),
            overlays[book.id],
        )
    }.toImmutableList()
}
