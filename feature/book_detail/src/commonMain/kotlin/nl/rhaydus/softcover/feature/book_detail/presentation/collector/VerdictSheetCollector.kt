package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.uibinding.verdict.toVerdictSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book, its review, its verdict cover and the open context to the verdict sheet's UI model off
 * the composition (R9), so `BookDetailOverlays` only ever forwards `BookDetailUiState.verdictSheet`.
 *
 * The model is `null` while `verdictSheetContext` is, which keeps that field the single open flag.
 */
internal class VerdictSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                VerdictSheetSnapshot(
                    book = it.book,
                    context = it.verdictSheetContext,
                    review = it.verdictReview,
                    cover = it.verdictCover,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val context = snapshot.context

                scope.setState {
                    it.copy(
                        verdictSheet = if (context == null) {
                            null
                        } else {
                            snapshot.book?.toVerdictSheetUiModel(
                                context = context,
                                review = snapshot.review,
                                cover = snapshot.cover,
                            )
                        },
                    )
                }
            }
    }
}
