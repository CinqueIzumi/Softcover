package nl.rhaydus.softcover.feature.book_detail.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.presentation.toad.ActionScope

class UserBooksFlowCollector :
    nl.rhaydus.softcover.feature.book_detail.presentation.flows.BookDetailInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState, nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent, nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables>,
        dependencies: nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies,
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