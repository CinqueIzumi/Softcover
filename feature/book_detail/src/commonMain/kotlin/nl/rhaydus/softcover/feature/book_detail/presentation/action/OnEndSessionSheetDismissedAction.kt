package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.core.presentation.session.stopSessionThen
import nl.rhaydus.softcover.core.uibinding.session.SessionEndingStatus
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnEndSessionSheetDismissedAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val confirmedStatus: SessionEndingStatus? = scope.currentState.endSessionSheet.dismissed()

        scope.setState {
            it.copy(
                endSessionSheet = SheetSession.Closed,
                endSessionRequest = null,
            )
        }

        if (confirmedStatus == null) return

        val book = scope.currentState.book ?: return

        dependencies.activeSessionController.stopSessionThen {
            val action: BookDetailAction = when (confirmedStatus) {
                SessionEndingStatus.Paused -> OnMarkBookAsPausedClickAction(
                    book = book,
                    sessionEndConfirmed = true,
                )

                SessionEndingStatus.DidNotFinish -> OnMarkBookAsDidNotFinishClickAction(
                    book = book,
                    sessionEndConfirmed = true,
                )
            }

            action.execute(
                dependencies = dependencies,
                scope = scope,
            )
        }
    }
}
