package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toDidNotFinishReasonSheet
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class DidNotFinishReasonSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book?.title to it.didNotFinishReasonDraft }
            .distinctUntilChanged()
            .collectLatest { (title, draft) ->
                scope.setState { state ->
                    state.copy(
                        didNotFinishReasonSheet = if (title == null || draft == null) {
                            state.didNotFinishReasonSheet.requestClose()
                        } else {
                            state.didNotFinishReasonSheet.openOrUpdate(
                                model = draft.toDidNotFinishReasonSheet(bookTitle = title),
                            )
                        },
                    )
                }
            }
    }
}
