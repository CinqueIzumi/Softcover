package nl.rhaydus.softcover.feature.library.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

class BookListsCollector : LibraryInitializer {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        combine(
            dependencies.getAllUserListsUseCase(),
            dependencies.getEnabledListIdsAsFlowUseCase(),
        ) { lists: List<BookList>, enabledIds: Set<Int> ->
            lists
                .filter { it.id in enabledIds }
                .associate { list ->
                    val tabId = LibraryTab.CustomList(listId = list.id, listName = list.name).id
                    tabId to list.books.mapNotNull { listBook ->
                        val edition = listBook.edition ?: return@mapNotNull null

                        if (edition.url == null && edition.localImagePath == null) {
                            edition.copy(url = listBook.book?.coverUrl)
                        } else {
                            edition
                        }
                    }
                }
        }.collectLatest { editionsPerList ->
            scope.setState { state ->
                val retainedKeys = editionsPerList.keys
                val stripped = state.editionsByTab.filterKeys { key -> key in retainedKeys }
                state.copy(editionsByTab = stripped + editionsPerList)
            }
        }
    }
}
