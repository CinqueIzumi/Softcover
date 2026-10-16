package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnQuoteDeleteConfirmedAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val localId = scope.currentState.quoteDeleteRequestId ?: return

        scope.setState { it.copy(quoteDeleteRequestId = null) }

        dependencies.launch {
            dependencies.deleteQuoteUseCase(localId = localId)
                .onFailure { error ->
                    AppLog.e("Failed to delete quote: $error")

                    SnackBarManager.showSnackbar(title = "Couldn't delete your quote. Try again.")

                    scope.sendEvent(BookDetailEvent.MutationFailedEvent())
                }
        }
    }
}
