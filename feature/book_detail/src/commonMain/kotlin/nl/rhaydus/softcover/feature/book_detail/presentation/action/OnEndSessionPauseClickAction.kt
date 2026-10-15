package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.persistentSetOf
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.core.uibinding.session.ConfirmedPause
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnEndSessionPauseClickAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                endSessionPauseSheet = it.endSessionPauseSheet.requestClose(
                    pending = ConfirmedPause(bookIds = persistentSetOf(it.bookId)),
                ),
                endSessionPauseRequested = false,
            )
        }
    }
}
