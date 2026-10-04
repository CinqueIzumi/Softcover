package nl.rhaydus.softcover.feature.explore.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.explore.presentation.event.ExploreEvent
import nl.rhaydus.softcover.feature.explore.presentation.mapper.FEATURED_CARD_STAND_IN
import nl.rhaydus.softcover.feature.explore.presentation.mapper.toFeaturedCard
import nl.rhaydus.softcover.feature.explore.presentation.screenmodel.ExploreDependencies
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreLocalVariables
import nl.rhaydus.softcover.feature.explore.presentation.state.ExploreScreenUiState
import nl.rhaydus.toad.ActionScope

/**
 * Assembles [ExploreScreenUiState.featuredCard] off the composition from the featured release, its
 * cover, its badge and its header. A card is [FEATURED_CARD_STAND_IN] while the release loads and `null`
 * once it has resolved with nothing to feature.
 */
internal class FeaturedCardCollector : ExploreCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ExploreScreenUiState, ExploreEvent, ExploreLocalVariables>,
        dependencies: ExploreDependencies,
    ) {
        scope.state
            .map {
                FeaturedCardSnapshot(
                    book = it.featuredUpcomingRelease,
                    cover = it.featuredCover,
                    badge = it.featuredReleaseBadge,
                    header = it.featuredHeader,
                    isLoading = it.loadingFeaturedUpcomingRelease,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        featuredCard = when {
                            snapshot.book != null -> snapshot.book.toFeaturedCard(
                                header = snapshot.header,
                                badge = snapshot.badge,
                                cover = snapshot.cover,
                            )

                            snapshot.isLoading -> FEATURED_CARD_STAND_IN.copy(header = snapshot.header)

                            else -> null
                        },
                    )
                }
            }
    }
}
