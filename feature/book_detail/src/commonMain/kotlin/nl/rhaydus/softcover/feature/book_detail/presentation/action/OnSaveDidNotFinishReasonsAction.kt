package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toDidNotFinishReasons
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/** Closes the sheet before the write, so a failed save loses the draft. */
internal class OnSaveDidNotFinishReasonsAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val draft = scope.currentState.didNotFinishReasonDraft ?: return

        val reasons = draft.toDidNotFinishReasons(bookId = scope.currentState.bookId)

        scope.setState { it.copy(didNotFinishReasonDraft = null) }

        dependencies.launch {
            dependencies.saveDidNotFinishReasonsUseCase(reasons)
                .onSuccess {
                    scope.sendEvent(BookDetailEvent.MutationSucceededEvent(feedback = Feedback.Routine))
                }
                .onFailure {
                    AppLog.e("Failed to save did not finish reasons: $it")

                    SnackBarManager.showSnackbar(title = "Couldn't save your reasons. Try again.")

                    scope.sendEvent(BookDetailEvent.MutationFailedEvent())
                }
        }
    }
}
