package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

class UserBooksFlowCollector : BookDetailInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        dependencies.getAllUserBooksUseCase().collectLatest { books ->
            val matchingBook = books
                .find { it.id == scope.currentState.book?.id } ?: return@collectLatest

            scope.setState {
                it.copy(book = matchingBook)
            }
        }
    }
}