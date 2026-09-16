package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.uibinding.release.UnreleasedBadgeStyle
import nl.rhaydus.softcover.core.uibinding.release.toUnreleasedBadgeUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps [BookDetailUiState.book]'s unreleased release date to a badge off the composition (R9).
 *
 * A separate collector rather than folding into an existing one because [BookDetailUiState.book] is
 * written by several collectors and actions — one writer per derived field keeps this badge from
 * going stale behind a forgotten `copy`.
 */
internal class UnreleasedBadgeCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book }
            .distinctUntilChanged()
            .collectLatest { book ->
                val badge = book
                    ?.takeIf { it.isUnreleased }
                    ?.effectiveReleaseDate
                    ?.toUnreleasedBadgeUiModel(UnreleasedBadgeStyle.Prominent)

                scope.setState { state ->
                    state.copy(unreleasedBadge = badge)
                }
            }
    }
}
