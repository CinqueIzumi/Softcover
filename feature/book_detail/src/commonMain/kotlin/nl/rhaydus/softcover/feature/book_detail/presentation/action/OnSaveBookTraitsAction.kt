package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toBookTraits
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/** Stores the draft; an empty one deletes the stored answers. The sheet closes at once, before the write. */
internal class OnSaveBookTraitsAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val draft = scope.currentState.howItReadDraft ?: return
        val traits = draft.toBookTraits(bookId = scope.currentState.bookId)

        scope.setState { it.copy(howItReadDraft = null) }

        dependencies.launch {
            dependencies.saveBookTraitsUseCase(traits)
                .onFailure { error ->
                    AppLog.e("Failed to save how it read: $error")

                    SnackBarManager.showSnackbar(title = "Couldn't save your answers. Try again.")

                    scope.sendEvent(BookDetailEvent.MutationFailedEvent())
                }
        }
    }
}
