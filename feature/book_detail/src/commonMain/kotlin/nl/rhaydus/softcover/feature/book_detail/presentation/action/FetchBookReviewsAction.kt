package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.BOOK_REVIEW_STAND_INS
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toBookReviewUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class FetchBookReviewsAction(
    val bookId: Int,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        dependencies.launch {
            scope.setState { it.copy(reviews = BOOK_REVIEW_STAND_INS) }

            val reviews = dependencies
                .getTopBookReviewsUseCase(bookId = bookId)
                .onApiFailure()
                .getOrDefault(emptyList())
                .map { it.toBookReviewUiModel() }
                .toImmutableList()

            scope.setState { it.copy(reviews = reviews) }
        }
    }
}
