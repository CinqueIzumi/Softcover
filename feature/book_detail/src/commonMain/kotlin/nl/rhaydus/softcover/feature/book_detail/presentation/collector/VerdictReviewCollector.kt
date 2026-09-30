package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.verdict.VerdictBlockUiModel
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the current user's rating and review document off the composition (R9), so the verdict block
 * and the verdict sheet only ever forward `BookDetailUiState.verdictBlock` and `verdictSheet` — never a
 * `ReviewDocument` for a composable to convert itself. Also writes `verdictReview`, the rich-text
 * review `VerdictSheetCollector` feeds the sheet mapper.
 */
internal class VerdictReviewCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book?.userBook }
            .distinctUntilChanged()
            .collectLatest { userBook ->
                val review = userBook?.reviewDocument?.toRichTextUiModel()

                scope.setState {
                    it.copy(
                        verdictReview = review,
                        verdictBlock = userBook?.let { book ->
                            VerdictBlockUiModel(
                                rating = book.rating?.takeIf { rating -> rating > 0.0 },
                                review = review,
                                hasSpoilers = book.reviewHasSpoilers,
                            )
                        },
                    )
                }
            }
    }
}
