package nl.rhaydus.softcover.feature.updated_library.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.updated_library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.updated_library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.updated_library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.updated_library.presentation.screenmodel.LibraryDependencies

class DidNotFinishBooksCollector : LibraryInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        dependencies.getDidNotFinishUserBooksUseCase().collectLatest { books: List<Book> ->
            scope.setState {
                it.copy(
                    dnfBooks = books,
                    isLoading = false,
                )
            }
        }
    }
}