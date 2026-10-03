package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.badge.BadgeSize
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.ownedBadge
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the hero's owned badge off the composition (R9). It reads
 * [BookDetailUiState.displayedEdition] and [BookDetailUiState.isEditionOwned], which are themselves
 * derived from several writers, so a dedicated collector keeps it from going stale behind a
 * forgotten `copy`.
 */
internal class EditionSelectionCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                EditionSelectionSnapshot(
                    isDisplayedEditionOwned = it.isEditionOwned(it.displayedEdition),
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        ownedCoverBadge = ownedBadge(size = BadgeSize.Regular).takeIf { snapshot.isDisplayedEditionOwned },
                    )
                }
            }
    }

    private data class EditionSelectionSnapshot(
        val isDisplayedEditionOwned: Boolean,
    )
}
