package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.uibinding.session.endSessionSheetScaffold
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class EndSessionSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { state ->
                Pair(
                    state.endSessionRequest,
                    state.book?.title,
                )
            }
            .distinctUntilChanged()
            .collectLatest { (requested, title) ->
                scope.setState {
                    it.copy(
                        endSessionSheet = if (requested != null && it.endSessionRequest != null) {
                            it.endSessionSheet.openOrUpdate(
                                model = endSessionSheetScaffold(
                                    bookTitle = title,
                                    targetStatus = requested,
                                ),
                            )
                        } else {
                            it.endSessionSheet.requestClose()
                        },
                    )
                }
            }
    }
}
