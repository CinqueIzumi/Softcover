package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toWhereYouGotItDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/** Seeds the where-you-got-it draft from the stored copy, which opens the sheet. */
internal class OnOpenWhereYouGotItSheetAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        if (scope.currentState.book == null) return

        dependencies.launch {
            dependencies.getBookCopyUseCase(bookId = scope.currentState.bookId)
                .onSuccess { copy ->
                    scope.setState { state ->
                        state.copy(whereYouGotItDraft = copy.toWhereYouGotItDraft())
                    }
                }
                .onFailure { error ->
                    AppLog.e("Failed to open where you got it: $error")

                    SnackBarManager.showSnackbar(title = "Couldn't load where you got it. Try again.")
                }
        }
    }
}
