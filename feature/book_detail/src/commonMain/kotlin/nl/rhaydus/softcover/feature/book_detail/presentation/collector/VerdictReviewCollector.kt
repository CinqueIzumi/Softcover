package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.verdict.VerdictBlockUiModel
import nl.rhaydus.softcover.core.domain.model.isBlank
import nl.rhaydus.softcover.core.uibinding.richtext.toRichTextUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the current user's rating and review document off the composition (R9), so the verdict block
 * only ever forwards `BookDetailUiState.verdictBlock` — never a `ReviewDocument` for a composable to
 * convert itself. The block is `null` while the book has no user data.
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
                val rating = userBook?.rating?.takeIf { it > 0.0 }
                val review = userBook?.reviewDocument?.takeUnless { it.isBlank() }?.toRichTextUiModel()

                scope.setState {
                    it.copy(
                        verdictBlock = when {
                            userBook == null -> null

                            rating == null && review == null -> VerdictBlockUiModel.Empty

                            else -> VerdictBlockUiModel.Filled(
                                rating = rating,
                                review = review,
                                hasSpoilers = userBook.reviewHasSpoilers,
                            )
                        },
                    )
                }
            }
    }
}
