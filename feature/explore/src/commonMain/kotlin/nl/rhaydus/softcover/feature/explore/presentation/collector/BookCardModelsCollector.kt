package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardRailUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.book.toBylineColumnCard
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.BECAUSE_YOU_READ_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.RECOMMENDED_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.SEARCH_RESULT_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.TRENDING_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.UP_NEXT_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toRailModel
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toSearchResultCard
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toTrendingCard
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toUpNextCard
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_RECOMMENDED
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_TRENDING
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT
import nl.rhaydus.softcover.feature.explore.presentation.screen.section.ExploreRail
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the recommended, trending, because-you-read and up-next rails and the search results to
 * [BookCardUiModel]s, one list per surface: the same book can sit on several surfaces, each stamping its
 * own cover transition key.
 *
 * Also derives the recommended section's `recommendedVisible` and the because-you-read section's `visible`
 * and `isLoading` in the same write as their card sets, so a section never renders a set that disagrees
 * with whether it is shown.
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
                    recommendedBooks = it.recommendedBooks,
                    becauseYouReadBooks = it.becauseYouReadBooks,
                    becauseYouReadGenre = it.becauseYouReadGenre,
                    continueSeriesBooks = it.continueSeriesBooks,
                    queriedBooks = it.queriedBooks,
                    unreleasedCoverOverlays = it.unreleasedCoverOverlays,
                    loadingTrendingBooks = it.loadingTrendingBooks,
                    loadingRecommendedBooks = it.loadingRecommendedBooks,
                    loadingBecauseYouReadBooks = it.loadingBecauseYouReadBooks,
                    loadingContinueSeriesBooks = it.loadingContinueSeriesBooks,
                    isLoading = it.isLoading,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        trending = snapshot.trendingBooks.toRailModel(
                            rail = ExploreRail.TRENDING,
                            loading = snapshot.loadingTrendingBooks,
                            coverSurface = SURFACE_TRENDING,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = TRENDING_STAND_IN_CARDS,
                            map = Book::toTrendingCard,
                        ),
                        recommended = snapshot.recommendedBooks.toRailModel(
                            rail = ExploreRail.RECOMMENDED,
                            loading = snapshot.loadingRecommendedBooks,
                            coverSurface = SURFACE_RECOMMENDED,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = RECOMMENDED_STAND_IN_CARDS,
                            map = Book::toBylineColumnCard,
                        ),
                        recommendedVisible = snapshot.loadingRecommendedBooks ||
                            snapshot.recommendedBooks.isNotEmpty(),
                        becauseYouRead = snapshot.becauseYouReadBooks.toRailModel(
                            rail = ExploreRail.BECAUSE_YOU_READ,
                            loading = snapshot.loadingBecauseYouReadBooks,
                            coverSurface = SURFACE_BECAUSE_YOU_READ,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = BECAUSE_YOU_READ_STAND_IN_CARDS,
                            map = Book::toBylineColumnCard,
                        ),
                        becauseYouReadGenreControl = it.becauseYouReadGenreControl.copy(
                            visible = snapshot.becauseYouReadGenre != null &&
                                (snapshot.loadingBecauseYouReadBooks || snapshot.becauseYouReadBooks.isNotEmpty()),
                            isLoading = snapshot.loadingBecauseYouReadBooks && snapshot.becauseYouReadBooks.isEmpty(),
                        ),
                        upNext = snapshot.continueSeriesBooks.toRailModel(
                            rail = ExploreRail.UP_NEXT,
                            loading = snapshot.loadingContinueSeriesBooks,
                            coverSurface = SURFACE_UP_NEXT,
                            overlays = snapshot.unreleasedCoverOverlays,
                            standIns = UP_NEXT_STAND_IN_CARDS,
                            map = Book::toUpNextCard,
                        ),
                        searchResults = snapshot.queriedBooks.toCardSet(
                            loading = snapshot.isLoading,
                            coverVariant = CoverVariant.Flat,
                            coverSurface = null,
                            standIns = SEARCH_RESULT_STAND_IN_CARDS,
                            map = { book, cover, _, surface ->
                                book.toSearchResultCard(
                                    cover = cover,
                                    transitionSurface = surface,
                                )
                            },
                        ),
                    )
                }
            }
    }
}

private inline fun List<Book>.toRailModel(
    rail: ExploreRail,
    loading: Boolean,
    coverSurface: String?,
    standIns: ImmutableList<BookCardUiModel>,
    overlays: Map<Int, CoverOverlayUiModel>,
    map: (Book, CoverUiModel, CoverOverlayUiModel?, String?) -> BookCardUiModel,
): BookCardRailUiModel<Book> = rail.toRailModel(
    set = toCardSet(
        loading = loading,
        coverVariant = CoverVariant.Raised,
        coverSurface = coverSurface,
        standIns = standIns,
        overlays = overlays,
        map = map,
    ),
)

private inline fun List<Book>.toCardSet(
    loading: Boolean,
    coverVariant: CoverVariant,
    coverSurface: String?,
    standIns: ImmutableList<BookCardUiModel>,
    overlays: Map<Int, CoverOverlayUiModel> = emptyMap(),
    map: (Book, CoverUiModel, CoverOverlayUiModel?, String?) -> BookCardUiModel,
): BookCardSet<Book> = if (loading && isEmpty()) {
    BookCardSet.standIns(standIns)
} else {
    map { book ->
        map(
            book,
            book.toCoverUiModel(
                variant = coverVariant,
                shareTransition = true,
                transitionSurface = coverSurface,
            ),
            overlays[book.id],
            coverSurface,
        ) to book
    }.toBookCardSet()
}
