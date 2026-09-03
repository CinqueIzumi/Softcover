package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the current user's review document to rich text off the composition (R9), so the verdict
 * block and the verdict sheet only ever forward `BookDetailUiState.verdictReview` — never a
 * `ReviewDocument` for a composable to convert itself.
 */
internal class VerdictReviewCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book?.userBook?.reviewDocument }
            .distinctUntilChanged()
            .collectLatest { reviewDocument ->
                scope.setState { it.copy(verdictReview = reviewDocument?.toRichTextUiModel()) }
            }
    }
}
