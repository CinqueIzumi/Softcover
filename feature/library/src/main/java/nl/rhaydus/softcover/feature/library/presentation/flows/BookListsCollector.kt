package nl.rhaydus.softcover.feature.library.presentation.flows

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
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
            val enabledLists = lists.filter { it.id in enabledIds }

            // Single pass per list: keep the `ListBook` entries whose edition resolved, then
            // derive both the edition list and the addedAt lookup from that one filtered slice so
            // they cannot drift apart if either guard is later changed.
            val renderedByTab: Map<String, List<ListBook>> = enabledLists.associate { list ->
                val tabId = LibraryTab.CustomList(listId = list.id, listName = list.name).id

                tabId to list.books.filter { it.edition != null }
            }

            val editionsPerList = renderedByTab.mapValues { (_, rendered) ->
                rendered.map { listBook ->
                    val edition = listBook.edition!!

                    if (edition.url == null && edition.localImagePath == null) {
                        edition.copy(url = listBook.book?.coverUrl)
                    } else {
                        edition
                    }
                }
            }

            val addedAtPerList: Map<String, Map<Int, String?>> = renderedByTab.mapValues { (_, rendered) ->
                rendered.associate { it.editionId to it.addedAt }
            }

            val booksById = enabledLists
                .flatMap { list -> list.books.mapNotNull { it.book } }
                .associateBy(Book::id)

            CollectedLists(
                editionsPerList = editionsPerList,
                addedAtPerList = addedAtPerList,
                booksById = booksById,
                allLists = lists,
            )
        }.collectLatest { collected ->
            scope.setState { state ->
                val retainedKeys = collected.editionsPerList.keys
                val strippedEditions = state.editionsByTab.filterKeys { it in retainedKeys }
                val strippedAddedAt = state.addedAtByTab.filterKeys { it in retainedKeys }
                state.copy(
                    editionsByTab = strippedEditions + collected.editionsPerList,
                    addedAtByTab = strippedAddedAt + collected.addedAtPerList,
                    bookByBookId = collected.booksById,
                    customLists = collected.allLists,
                )
            }
        }
    }
}
