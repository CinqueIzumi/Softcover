package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.feature.profile.presentation.event.ProfileEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.RECENTLY_LOVED_CARD_COUNT
import nl.rhaydus.softcover.feature.profile.presentation.mapper.RECENTLY_LOVED_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRecentlyLovedCard
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.ProfileDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.LocalProfileVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.ProfileUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the reader's most recently loved books to book cards off the composition. While the reading
 * life is still loading and there are none, the cards are loading stand-ins.
 */
internal class RecentlyLovedCardsCollector : ProfileCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ProfileUiState, ProfileEvent, LocalProfileVariables>,
        dependencies: ProfileDependencies,
    ) {
        scope.state
            .map {
                RecentlyLovedSnapshot(
                    books = it.readingLife?.recentlyLoved.orEmpty(),
                    isLoading = it.isReadingLifeLoading,
                )
            }
            .distinctUntilChanged()
            .collect { snapshot ->
                scope.setState {
                    it.copy(
                        recentlyLoved = if (snapshot.isLoading && snapshot.books.isEmpty()) {
                            BookCardSet.standIns(RECENTLY_LOVED_STAND_IN_CARDS)
                        } else {
                            snapshot.books
                                .take(RECENTLY_LOVED_CARD_COUNT)
                                .map { book -> book.toRecentlyLovedCard() to book }
                                .toBookCardSet()
                        },
                    )
                }
            }
    }
}
