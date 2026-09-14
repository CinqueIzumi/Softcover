package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_BECAUSE_YOU_READ
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_FEATURED
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_TRENDING
import nl.rhaydus.softcover.feature.explore.presentation.screen.SURFACE_UP_NEXT
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps every book-carrying rail to its cover UI models off the composition (`component-contract.md`
 * § 7.2 R9), deriving off [ActionScope.state] rather than being patched into each writer: six
 * actions/collectors write across the five source lists this reads (`TrendingBooksCollector` and
 * `OnRefreshAction` both write `trendingBooks`; `QueriedBooksCollector` and `ExploreSearchExecution`
 * both write `queriedBooks`; `BecauseYouReadCollector` and `OnBecauseYouReadGenreSelectedAction` both
 * write `becauseYouReadBooks`) — a derived field patched into each write site would be one forgotten
 * `copy` from a stale cover.
 *
 * Five separate maps/fields rather than one keyed by book id: the same book can appear in more than
 * one rail at once, and each rail stamps a different `sharedTransitionKey` surface.
 */
internal class CoverModelsCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map {
                CoverModelsSnapshot(
                    featuredUpcomingRelease = it.featuredUpcomingRelease,
                    trendingBooks = it.trendingBooks,
                    becauseYouReadBooks = it.becauseYouReadBooks,
                    continueSeriesBooks = it.continueSeriesBooks,
                    queriedBooks = it.queriedBooks,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        featuredCover = snapshot.featuredUpcomingRelease?.toCoverModel(
                            variant = CoverVariant.ExploreRail,
                            surface = SURFACE_FEATURED,
                        ),
                        trendingCovers = snapshot.trendingBooks.toCoverModels(
                            variant = CoverVariant.ExploreRail,
                            surface = SURFACE_TRENDING,
                        ),
                        becauseYouReadCovers = snapshot.becauseYouReadBooks.toCoverModels(
                            variant = CoverVariant.ExploreRail,
                            surface = SURFACE_BECAUSE_YOU_READ,
                        ),
                        continueSeriesCovers = snapshot.continueSeriesBooks.toCoverModels(
                            variant = CoverVariant.ExploreSeriesCard,
                            surface = SURFACE_UP_NEXT,
                        ),
                        queriedBookCovers = snapshot.queriedBooks.toCoverModels(
                            variant = CoverVariant.ExploreSearchRow,
                            surface = null,
                        ),
                    )
                }
            }
    }
}

private fun Book.toCoverModel(
    variant: CoverVariant,
    surface: String?,
): CoverUiModel = toCoverUiModel(
    variant = variant,
    sharedTransitionKey = bookCoverTransitionKey(
        editionId = currentEdition?.id,
        bookId = id,
        surface = surface,
    ),
)

private fun List<Book>.toCoverModels(
    variant: CoverVariant,
    surface: String?,
): Map<Int, CoverUiModel> = associate { book ->
    book.id to book.toCoverModel(
        variant = variant,
        surface = surface,
    )
}
