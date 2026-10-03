package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRecentlyLovedCard
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

internal const val RECENTLY_LOVED_CARD_COUNT = 3

/** Maps the reader's most recently loved books to book cards off the composition. */
internal class RecentlyLovedCardsCollector : ProfileCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
        dependencies: ProfileDependencies,
    ) {
        scope.state
            .map { it.readingLife?.recentlyLoved.orEmpty() }
            .distinctUntilChanged()
            .collect { books ->
                scope.setState {
                    it.copy(
                        recentlyLovedCards = books
                            .take(RECENTLY_LOVED_CARD_COUNT)
                            .map(LovedBook::toRecentlyLovedCard)
                            .toImmutableList(),
                    )
                }
            }
    }
}
