package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.badge.CoverOverlayCorner
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.release.toCompactUnreleasedBadgeUiModel
import nl.rhaydus.softcover.core.uibinding.release.toFeaturedUnreleasedBadgeUiModel
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps every book-carrying rail to its unreleased-cover-overlay UI models off the composition
 * (`component-contract.md` § 7.2 R9), deriving off [ActionScope.state] rather than being patched
 * into each writer. Some rails have more than one writer (`QueriedBooksCollector` and
 * `ExploreSearchExecution` both write `queriedBooks`; `BecauseYouReadCollector` and
 * `OnBecauseYouReadGenreSelectedAction` both write `becauseYouReadBooks`), and a derived field patched
 * into each write site would be one forgotten `copy` from a stale overlay.
 *
 * One map keyed by book id, unlike the covers [BookCardModelsCollector] builds per rail: a cover carries a per-rail
 * `sharedTransitionKey` surface, so the same book yields a different cover per rail. A
 * [CoverOverlayUiModel] carries no such surface, so the same book yields an identical one everywhere it
 * appears. One id-keyed map is the single source of truth; one per rail would just be more ways for the
 * same book's overlay to disagree with itself.
 */
internal class UnreleasedCoverOverlaysCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map {
                UnreleasedBadgeModelsSnapshot(
                    featuredUpcomingRelease = it.featuredUpcomingRelease,
                    trendingBooks = it.trendingBooks,
                    recommendedBooks = it.recommendedBooks,
                    becauseYouReadBooks = it.becauseYouReadBooks,
                    continueSeriesBooks = it.continueSeriesBooks,
                    queriedBooks = it.queriedBooks,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        unreleasedCoverOverlays = (
                            snapshot.trendingBooks +
                                snapshot.recommendedBooks +
                                snapshot.becauseYouReadBooks +
                                snapshot.continueSeriesBooks +
                                snapshot.queriedBooks
                        ).toUnreleasedCoverOverlays().toImmutableMap(),
                        // The book's own debut, the date the featured query filters on: an edition's
                        // date can fall outside the window the card promises. No isUnreleased gate,
                        // since that query only returns unreleased books.
                        featuredReleaseBadge = snapshot.featuredUpcomingRelease
                            ?.releaseDate
                            ?.toFeaturedUnreleasedBadgeUiModel(),
                    )
                }
            }
    }
}

private fun List<Book>.toUnreleasedCoverOverlays(): Map<Int, CoverOverlayUiModel> =
    mapNotNull { book ->
        val releaseDate = book.releaseDate
        if (book.isUnreleased && releaseDate != null) {
            book.id to CoverOverlayUiModel(
                badge = releaseDate.toCompactUnreleasedBadgeUiModel(),
                corner = CoverOverlayCorner.TopStart,
                grayscale = false,
            )
        } else {
            null
        }
    }.toMap()
