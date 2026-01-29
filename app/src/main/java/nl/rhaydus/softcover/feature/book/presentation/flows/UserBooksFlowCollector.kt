package nl.rhaydus.softcover.feature.book.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailDependencies

class UserBooksFlowCollector : BookDetailFlowCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        dependencies.getUserBooksAsFlowUseCase().collectLatest { books ->
            val matchingBook =
                books.find { it.id == scope.currentState.book?.id } ?: return@collectLatest

            scope.setState {
                it.copy(book = matchingBook)
            }
        }
    }
}