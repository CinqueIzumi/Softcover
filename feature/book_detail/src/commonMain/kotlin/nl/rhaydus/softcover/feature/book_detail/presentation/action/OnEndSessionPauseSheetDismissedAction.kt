package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.uibinding.session.stopSessionThen
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnEndSessionPauseSheetDismissedAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val confirmed = scope.currentState.endSessionPauseSheet.dismissed()

        scope.setState {
            it.copy(
                endSessionPauseSheet = SheetSession.Closed,
                endSessionPauseRequested = false,
            )
        }

        if (confirmed == null) return

        val book = scope.currentState.book ?: return

        dependencies.activeSessionController.stopSessionThen {
            OnMarkBookAsPausedClickAction(
                book = book,
                sessionPauseConfirmed = true,
            ).execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
