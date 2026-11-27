package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.component.haptics.Feedback
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toSource
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Stores the draft over the stored copy, so fields this sheet does not edit survive; an empty draft clears
 * the source. The sheet closes at once, before the write.
 */
internal class OnSaveWhereYouGotItAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val draft = scope.currentState.whereYouGotItDraft ?: return

        val source = draft.toSource()

        scope.setState { it.copy(whereYouGotItDraft = null) }

        dependencies.launch {
            val stored = dependencies.getBookCopyUseCase(bookId = scope.currentState.bookId)
                .onFailure {
                    reportFailure(
                        error = it,
                        scope = scope,
                    )
                }
                .getOrNull() ?: return@launch

            dependencies.saveBookCopyUseCase(stored.copy(source = source))
                .onSuccess {
                    scope.sendEvent(BookDetailEvent.MutationSucceededEvent(feedback = Feedback.Routine))
                }
                .onFailure {
                    reportFailure(
                        error = it,
                        scope = scope,
                    )
                }
        }
    }

    private fun reportFailure(
        error: Throwable,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        AppLog.e("Failed to save where you got it: $error")

        SnackBarManager.showSnackbar(title = "Couldn't save where you got it. Try again.")

        scope.sendEvent(BookDetailEvent.MutationFailedEvent())
    }
}
