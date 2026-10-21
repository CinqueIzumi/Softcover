package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.core.component.bookcard.toBookCardSet
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.mapper.RECOMMENDED_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.library.presentation.mapper.toRecommendedCard
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

private const val RECOMMENDED_SHELF_LIMIT = 5

private val WANT_TO_READ_TAB_ID = LibraryTab.Status.idFor(status = UserBookStatus.WANT_TO_READ)

internal class RecommendedRailCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                RecommendedRailSnapshot(
                    wantToReadCount = state.wantToReadCount(),
                    recommendedBooks = state.recommendedBooks,
                    loadingRecommendedBooks = state.loadingRecommendedBooks,
                    selectionMode = state.selectionMode,
                    isRearranging = state.isRearranging,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(recommendedRailByTab = snapshot.toRails())
                }
            }
    }
}

private fun LibraryUiState.wantToReadCount(): Int? {
    if (visibleTabs.any { it.id == WANT_TO_READ_TAB_ID }.not()) return null

    val books = booksByTab[WANT_TO_READ_TAB_ID] ?: return null

    return books.size.takeUnless { books.isEmpty() && isLoading }
}

private fun RecommendedRailSnapshot.toRails(): ImmutableMap<String, BookCardSet<Book>> {
    val shelfHasRoom = wantToReadCount != null && wantToReadCount < RECOMMENDED_SHELF_LIMIT
    val idle = selectionMode.not() && isRearranging.not()
    val hasContent = loadingRecommendedBooks || recommendedBooks.isNotEmpty()

    if (shelfHasRoom.not() || idle.not() || hasContent.not()) return persistentMapOf()

    val set = if (recommendedBooks.isEmpty()) {
        BookCardSet.standIns(RECOMMENDED_STAND_IN_CARDS)
    } else {
        recommendedBooks.map { book -> book.toRecommendedCard() to book }.toBookCardSet()
    }

    return persistentMapOf(WANT_TO_READ_TAB_ID to set)
}
