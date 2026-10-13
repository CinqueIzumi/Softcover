package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.lensToggleFor
import nl.rhaydus.toad.ActionScope

internal class LensToggleCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.selectedLens to (it.book?.userBook != null) }
            .distinctUntilChanged()
            .collectLatest { (selectedLens, yoursEnabled) ->
                val toggle = lensToggleFor(
                    selectedLens = selectedLens,
                    yoursEnabled = yoursEnabled,
                )

                scope.setState { state -> state.copy(lensToggle = toggle) }
            }
    }
}
