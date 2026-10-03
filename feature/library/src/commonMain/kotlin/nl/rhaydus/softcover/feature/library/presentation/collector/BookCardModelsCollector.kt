package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.mapper.toBookCard
import nl.rhaydus.softcover.feature.library.presentation.mapper.toEditionCard
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.bookReorderStatusFor
import nl.rhaydus.softcover.feature.library.presentation.state.isEditionListRearranging
import nl.rhaydus.softcover.feature.library.presentation.state.resolveSortMode
import nl.rhaydus.toad.ActionScope

/**
 * Maps every book and edition on every Library layout to a [BookCardUiModel] off the composition
 * (`component-contract.md` § 7.2 R9), keyed by tab id then by [Book.id] or edition id into
 * [LibraryUiState.bookCardsByTab] and [LibraryUiState.editionCardsByTab].
 *
 * Only tabs in [LibraryUiState.visibleTabs] are mapped. A tab whose inputs are unchanged since the
 * last run keeps its previous inner map, so a selection toggle rebuilds only the tab it touches.
 *
 * Reads the rearrange rule through [bookReorderStatusFor] and [isEditionListRearranging], the same
 * functions the list composables use, so a card is inert exactly when its list is in drag-only mode.
 */
internal class BookCardModelsCollector : LibraryCollector {
    private var bookCache: Map<String, CachedTab<BookTabInputs>> = emptyMap()
    private var editionCache: Map<String, CachedTab<EditionTabInputs>> = emptyMap()

    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                BookCardModelsSnapshot(
                    visibleTabs = state.visibleTabs,
                    booksByTab = state.booksByTab,
                    editionsByTab = state.editionsByTab,
                    gridLayout = state.gridLayout,
                    deadlines = state.deadlines,
                    dateStyle = state.dateStyle,
                    selectionMode = state.selectionMode,
                    selectedBookIds = state.selectedBookIds,
                    isRearranging = state.isRearranging,
                    sortModeByTab = state.sortModeByTab,
                    customLists = state.customLists,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val (books, editions) = withContext(dependencies.defaultDispatcher) {
                    snapshot.toBookCards(previous = bookCache) to
                        snapshot.toEditionCards(previous = editionCache)
                }

                bookCache = books
                editionCache = editions

                scope.setState {
                    it.copy(
                        bookCardsByTab = books.mapValues { (_, tab) -> tab.cards }.toImmutableMap(),
                        editionCardsByTab = editions.mapValues { (_, tab) -> tab.cards }.toImmutableMap(),
                    )
                }
            }
    }
}

/** Reuses [previous] when its inputs equal [inputs]; otherwise rebuilds the cards from [build]. */
private inline fun <I> cachedTab(
    previous: CachedTab<I>?,
    inputs: I,
    build: () -> List<Pair<Int, BookCardUiModel>>,
): CachedTab<I> = if (previous != null && previous.inputs == inputs) {
    previous
} else {
    CachedTab(
        inputs = inputs,
        cards = build().toMap().toImmutableMap(),
    )
}

private fun BookCardModelsSnapshot.toBookCards(
    previous: Map<String, CachedTab<BookTabInputs>>,
): Map<String, CachedTab<BookTabInputs>> = buildMap {
    val backdrop = if (gridLayout == LibraryGridLayout.LIST_COMPACT) {
        SelectionMarkBackdrop.Surface
    } else {
        SelectionMarkBackdrop.Image
    }

    visibleTabs.forEach { tab ->
        val books = booksByTab[tab.id] ?: return@forEach

        val rearranging = bookReorderStatusFor(
            tab = tab,
            sortMode = resolveSortMode(
                sortModeByTab = sortModeByTab,
                tabId = tab.id,
            ),
            selectionMode = selectionMode,
            isRearranging = isRearranging,
        ) != null

        val press = when {
            rearranging -> BookCardDecorations.Press.None
            selectionMode -> BookCardDecorations.Press.Tap
            else -> BookCardDecorations.Press.TapAndLongPress
        }

        val inputs = BookTabInputs(
            books = books,
            deadlines = books.map { deadlines[it.id] },
            dateStyle = dateStyle,
            marks = books.map { book ->
                if (selectionMode) {
                    SelectionMarkUiModel(
                        selected = book.id in selectedBookIds,
                        backdrop = backdrop,
                    )
                } else {
                    null
                }
            },
            press = press,
            layout = gridLayout,
        )

        put(
            tab.id,
            cachedTab(
                previous = previous[tab.id],
                inputs = inputs,
            ) {
                books.mapIndexed { index, book ->
                    book.id to book.toBookCard(
                        layout = inputs.layout,
                        press = press,
                        selection = inputs.marks[index],
                        deadline = inputs.deadlines[index],
                        dateStyle = inputs.dateStyle,
                    )
                }
            },
        )
    }
}

private fun BookCardModelsSnapshot.toEditionCards(
    previous: Map<String, CachedTab<EditionTabInputs>>,
): Map<String, CachedTab<EditionTabInputs>> = buildMap {
    visibleTabs.forEach { tab ->
        val editions = editionsByTab[tab.id] ?: return@forEach
        val listId = (tab as? LibraryTab.CustomList)?.listId

        val rearranging = isEditionListRearranging(
            sortMode = resolveSortMode(
                sortModeByTab = sortModeByTab,
                tabId = tab.id,
            ),
            isRanked = customLists.firstOrNull { it.id == listId }?.ranked == true,
            isRearranging = isRearranging,
        )

        val press = if (rearranging) BookCardDecorations.Press.None else BookCardDecorations.Press.Tap

        val inputs = EditionTabInputs(
            editions = editions,
            press = press,
            layout = gridLayout,
        )

        put(
            tab.id,
            cachedTab(
                previous = previous[tab.id],
                inputs = inputs,
            ) {
                editions.map { edition ->
                    edition.id to edition.toEditionCard(
                        layout = inputs.layout,
                        press = press,
                    )
                }
            },
        )
    }
}
