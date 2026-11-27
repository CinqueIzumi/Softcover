package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toBookMoods
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnSaveBookMoodsAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val draft = scope.currentState.howItFeltDraft ?: return

        val moods = draft.toBookMoods()

        scope.setState { it.copy(howItFeltDraft = null) }

        dependencies.launch {
            dependencies.saveBookMoodsUseCase(
                bookId = scope.currentState.bookId,
                moods = moods,
            ).onFailure { error ->
                AppLog.e("Failed to save how it felt: $error")

                SnackBarManager.showSnackbar(title = "Couldn't save how it felt. Try again.")

                scope.sendEvent(BookDetailEvent.MutationFailedEvent())
            }
        }
    }
}
