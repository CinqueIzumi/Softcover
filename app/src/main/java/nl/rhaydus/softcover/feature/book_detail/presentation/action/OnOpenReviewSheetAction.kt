package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.presentation.util.htmlToAnnotatedString
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

class OnOpenReviewSheetAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState { state ->
            val userBook = state.book?.userBook

            // Hardcover stores the review as HTML; seed the editor with its plain-text rendering so
            // the user edits words, not markup.
            val seedBody: String = userBook?.review
                ?.let { htmlToAnnotatedString(html = it).text }
                .orEmpty()

            state.copy(
                showReviewSheet = true,
                reviewEditorBody = seedBody,
                reviewEditorHasSpoilers = userBook?.reviewHasSpoilers ?: false,
            )
        }
    }
}
