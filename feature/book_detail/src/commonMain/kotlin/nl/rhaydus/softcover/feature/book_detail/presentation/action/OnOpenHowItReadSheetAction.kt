package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toHowItReadDraft
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/** Seeds the how-it-read draft from the stored answers, which opens the sheet. */
internal class OnOpenHowItReadSheetAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        if (scope.currentState.book == null) return

        dependencies.launch {
            dependencies.getBookTraitsUseCase(bookId = scope.currentState.bookId)
                .onSuccess { traits ->
                    scope.setState { state ->
                        state.copy(howItReadDraft = traits.toHowItReadDraft())
                    }
                }
                .onFailure { error ->
                    AppLog.e("Failed to open how it read: $error")

                    SnackBarManager.showSnackbar(title = "Couldn't load your answers. Try again.")
                }
        }
    }
}
