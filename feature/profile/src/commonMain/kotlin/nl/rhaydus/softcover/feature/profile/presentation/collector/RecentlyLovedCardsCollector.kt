package nl.rhaydus.softcover.feature.profile.presentation.collector

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.feature.profile.presentation.event.StatsAtlasEvent
import nl.rhaydus.softcover.feature.profile.presentation.mapper.RECENTLY_LOVED_CARD_COUNT
import nl.rhaydus.softcover.feature.profile.presentation.mapper.RECENTLY_LOVED_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.profile.presentation.mapper.toRecentlyLovedCard
import nl.rhaydus.softcover.feature.profile.presentation.screenmodel.StatsAtlasDependencies
import nl.rhaydus.softcover.feature.profile.presentation.state.RecentlyLovedEntry
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasLocalVariables
import nl.rhaydus.softcover.feature.profile.presentation.state.StatsAtlasUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the reader's most recently loved books to book cards off the composition. Each card takes its
 * cover from the cached library book when there is one, and re-maps when that book changes. While the
 * reading life is still loading and there are none, the cards are loading stand-ins.
 */
internal class RecentlyLovedCardsCollector : StatsAtlasCollector {
    override suspend fun onLaunch(
        scope: ActionScope<StatsAtlasUiState, StatsAtlasEvent, StatsAtlasLocalVariables>,
        dependencies: StatsAtlasDependencies,
    ) {
        scope.state
            .map {
                RecentlyLovedSnapshot(
                    books = it.readingLife?.recentlyLoved.orEmpty(),
                    isLoading = it.isReadingLifeLoading,
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { snapshot -> snapshot.toCardSets(dependencies) }
            .collect { cards -> scope.setState { it.copy(recentlyLoved = cards) } }
    }

    private fun RecentlyLovedSnapshot.toCardSets(
        dependencies: StatsAtlasDependencies,
    ): Flow<BookCardSet<RecentlyLovedEntry>> {
        if (isLoading && books.isEmpty()) {
            return flowOf(BookCardSet.standIns(RECENTLY_LOVED_STAND_IN_CARDS))
        }

        val loved = books.take(RECENTLY_LOVED_CARD_COUNT)
        if (loved.isEmpty()) return flowOf(BookCardSet.empty())

        return dependencies.observeBooksByIdsUseCase(loved.map { it.bookId }).map { libraryBooks ->
            val byId = libraryBooks.associateBy { it.id }

            loved
                .map { book ->
                    val entry = RecentlyLovedEntry(
                        loved = book,
                        libraryBook = byId[book.bookId]?.takeIf { it.userBook != null },
                    )

                    entry.toRecentlyLovedCard() to entry
                }
                .toBookCardSet()
        }
    }
}
