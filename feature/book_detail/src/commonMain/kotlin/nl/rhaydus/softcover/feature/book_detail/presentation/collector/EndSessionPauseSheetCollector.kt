package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.uibinding.session.endSessionPauseSheetScaffold
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class EndSessionPauseSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { state ->
                Pair(
                    state.endSessionPauseRequested,
                    state.book?.title,
                )
            }
            .distinctUntilChanged()
            .collectLatest { (requested, title) ->
                scope.setState {
                    it.copy(
                        endSessionPauseSheet = if (requested && it.endSessionPauseRequested) {
                            it.endSessionPauseSheet.openOrUpdate(
                                model = endSessionPauseSheetScaffold(bookTitle = title),
                            )
                        } else {
                            it.endSessionPauseSheet.requestClose()
                        },
                    )
                }
            }
    }
}
