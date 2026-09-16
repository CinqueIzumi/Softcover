package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.release.UnreleasedBadgeStyle
import nl.rhaydus.softcover.core.uibinding.release.toUnreleasedBadgeUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps every book-carrying rail to its unreleased-badge UI models off the composition
 * (`component-contract.md` § 7.2 R9), deriving off [ActionScope.state] rather than being patched
 * into each writer: the same six actions/collectors [CoverModelsCollector] already guards against
 * write across the five source lists this reads (`TrendingBooksCollector` and `OnRefreshAction` both
 * write `trendingBooks`; `QueriedBooksCollector` and `ExploreSearchExecution` both write
 * `queriedBooks`; `BecauseYouReadCollector` and `OnBecauseYouReadGenreSelectedAction` both write
 * `becauseYouReadBooks`) — a derived field patched into each write site would be one forgotten
 * `copy` from a stale badge.
 *
 * One map keyed by book id, unlike [CoverModelsCollector]'s five: a cover carries a per-rail
 * `sharedTransitionKey` surface, so the same book yields a different cover per rail. A badge carries
 * no such surface — the same book yields an identical [BadgeUiModel] everywhere it appears — so one
 * id-keyed map is the single source of truth; five would just be five ways for the same book's badge
 * to disagree with itself.
 */
internal class UnreleasedBadgeModelsCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map {
                UnreleasedBadgeModelsSnapshot(
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
                        unreleasedBadges = (
                            snapshot.trendingBooks +
                                snapshot.becauseYouReadBooks +
                                snapshot.continueSeriesBooks +
                                snapshot.queriedBooks
                        ).toUnreleasedBadgeModels(),
                        featuredReleaseBadge = snapshot.featuredUpcomingRelease
                            ?.effectiveReleaseDate
                            ?.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Featured),
                    )
                }
            }
    }
}

private fun List<Book>.toUnreleasedBadgeModels(): Map<Int, BadgeUiModel> =
    mapNotNull { book ->
        val releaseDate = book.effectiveReleaseDate
        if (book.isUnreleased && releaseDate != null) {
            book.id to releaseDate.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Compact)
        } else {
            null
        }
    }.toMap()
