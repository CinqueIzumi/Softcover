package nl.rhaydus.softcover.feature.search.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.search.presentation.viewmodel.SearchDependencies

class QueriedBooksCollector() : SearchInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<SearchScreenUiState, SearchEvent, SearchLocalVariables>,
        dependencies: SearchDependencies,
    ) {
        combine(
            dependencies.getAllUserBooksUseCase(),
            dependencies.getQueriedBooksUseCase()
        ) { allUserBooks: List<Book>, fetchedBooks: List<Book> ->
            fetchedBooks.map {
                allUserBooks.find { userBook -> userBook.id == it.id } ?: it
            }
        }.collectLatest { queriedBooks: List<Book> ->
            scope.setState { it.copy(queriedBooks = queriedBooks) }
        }
    }
}