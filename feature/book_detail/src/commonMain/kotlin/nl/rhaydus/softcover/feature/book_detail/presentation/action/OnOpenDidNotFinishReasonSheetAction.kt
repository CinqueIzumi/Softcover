package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toDidNotFinishReasonDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnOpenDidNotFinishReasonSheetAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        if (scope.currentState.book == null) return

        dependencies.launch {
            dependencies.getDidNotFinishReasonsUseCase(bookId = scope.currentState.bookId)
                .onSuccess { stored ->
                    scope.setState { state ->
                        state.copy(didNotFinishReasonDraft = stored.toDidNotFinishReasonDraft())
                    }
                }
                .onFailure { error ->
                    AppLog.e("Failed to open did not finish reasons: $error")

                    SnackBarManager.showSnackbar(title = "Couldn't load your reasons. Try again.")
                }
        }
    }
}
