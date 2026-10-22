package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.domain.model.ListBook
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.CollectedLists
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class BookListsCollector : LibraryCollector {
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
            val renderedByTab: Map<String, List<Pair<ListBook, BookEdition>>> = enabledLists.associate { list ->
                val tabId = LibraryTab.CustomList(
                    listId = list.id,
                    listName = list.name,
                ).id

                tabId to list.books.mapNotNull { listBook ->
                    listBook.shownEdition?.let { edition -> listBook to edition }
                }
            }

            val editionsPerList = renderedByTab.mapValues { (_, rendered) ->
                rendered.map { (listBook, edition) ->
                    if (edition.url == null && edition.localImagePath == null) {
                        edition.copy(url = listBook.book?.coverUrl)
                    } else {
                        edition
                    }
                }
            }

            val addedAtPerList: Map<String, Map<Int, String?>> = renderedByTab.mapValues { (_, rendered) ->
                rendered.associate { (listBook, edition) -> edition.id to listBook.addedAt }
            }

            val booksById = enabledLists
                .flatMap { list -> list.books.mapNotNull { it.book } }
                .associateBy(Book::id)

            CollectedLists(
                editionsPerList = editionsPerList.mapValues { (_, editions) -> editions.toImmutableList() }.toImmutableMap(),
                addedAtPerList = addedAtPerList.mapValues { (_, addedAt) -> addedAt.toImmutableMap() }.toImmutableMap(),
                booksById = booksById.toImmutableMap(),
                allLists = lists.toImmutableList(),
            )
        }.collectLatest { collected ->
            scope.setState { state ->
                val retainedKeys = collected.editionsPerList.keys
                val strippedEditions = state.editionsByTab.filterKeys { it in retainedKeys }
                val strippedAddedAt = state.addedAtByTab.filterKeys { it in retainedKeys }
                state.copy(
                    editionsByTab = strippedEditions.toPersistentMap().putAll(collected.editionsPerList),
                    addedAtByTab = strippedAddedAt.toPersistentMap().putAll(collected.addedAtPerList),
                    bookByBookId = collected.booksById,
                    customLists = collected.allLists,
                )
            }
        }
    }
}
