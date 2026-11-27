package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailMoodMomentSheetHost
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal data class OnMoodMomentClickedAction(val id: String) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val momentId = id.toLongOrNull() ?: return

        dependencies.moodMomentSheetSlice.openToEdit(
            bookId = scope.currentState.bookId,
            momentId = momentId,
            host = BookDetailMoodMomentSheetHost,
            scope = scope,
        )
    }
}
