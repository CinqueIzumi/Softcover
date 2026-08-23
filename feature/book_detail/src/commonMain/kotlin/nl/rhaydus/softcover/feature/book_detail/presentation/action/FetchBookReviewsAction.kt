package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
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
            scope.setState {
                it.copy(
                    loadingReviews = true,
                    revealedSpoilerReviewIds = emptySet(),
                )
            }

            val reviews = dependencies
                .getTopBookReviewsUseCase(bookId = bookId)
                .onApiFailure()
                .getOrDefault(emptyList())

            scope.setState {
                it.copy(
                    reviews = reviews,
                    loadingReviews = false,
                )
            }
        }
    }
}
