package nl.rhaydus.softcover.feature.reading.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenDependencies

class CurrentlyReadingBooksCollector : ReadingFlowCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent>,
        dependencies: ReadingScreenDependencies,
    ) {
        dependencies.getCurrentlyReadingBooksUseCase().collectLatest { books: List<Book> ->
            scope.setState {
                copy(
                    books = books,
                    isLoading = false,
                )
            }
        }
    }
}