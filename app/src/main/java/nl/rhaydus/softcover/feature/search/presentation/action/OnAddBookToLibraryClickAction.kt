package nl.rhaydus.softcover.feature.search.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.search.presentation.event.SearchEvent
import nl.rhaydus.softcover.feature.search.presentation.state.SearchLocalVariables
import nl.rhaydus.softcover.feature.search.presentation.state.SearchScreenUiState
import nl.rhaydus.softcover.feature.search.presentation.viewmodel.SearchDependencies
import timber.log.Timber

class OnAddBookToLibraryClickAction(val book: Book) : SearchAction {
    override suspend fun execute(
        dependencies: SearchDependencies,
        scope: ActionScope<SearchScreenUiState, SearchEvent, SearchLocalVariables>,
    ) {
        dependencies.markBookAsWantToReadUseCase(bookId = book.id).onFailure {
            Timber.e("-=-= $it")
        }
    }
}