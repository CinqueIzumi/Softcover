package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.component.bookcard.BookCardContent
import nl.rhaydus.softcover.core.component.bookcard.BookCardDecorations
import nl.rhaydus.softcover.core.component.bookcard.BookCardKey
import nl.rhaydus.softcover.core.component.bookcard.BookCardProgress
import nl.rhaydus.softcover.core.component.bookcard.BookCardUiModel
import nl.rhaydus.softcover.core.component.bookcard.BookCardVariant
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.bookReorderStatusFor
import nl.rhaydus.softcover.feature.library.presentation.state.isEditionListRearranging
import nl.rhaydus.softcover.feature.library.presentation.state.resolveSortMode
import nl.rhaydus.toad.ActionScope

/**
 * Maps every book and edition on the Library's grid layouts to a [BookCardUiModel] off the
 * composition (`component-contract.md` § 7.2 R9), keyed by tab id then by [Book.id] or edition id
 * into [LibraryUiState.bookCardsByTab] and [LibraryUiState.editionCardsByTab].
 * [LibraryGridLayout.GRID_TWO_COLUMNS] and [LibraryGridLayout.GRID_THREE_COLUMNS] map to
 * [BookCardVariant.Stacked]; the cover-only layouts to [BookCardVariant.CoverOnly]. Both maps are
 * empty on the list layouts, which keep their own rows.
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
                    bookCovers = state.bookCovers,
                    editionCovers = state.editionCovers,
                    selectionMarks = state.selectionMarks,
                    deadlineCountdownOverlays = state.deadlineCountdownOverlays,
                    selectionMode = state.selectionMode,
                    isRearranging = state.isRearranging,
                    sortModeByTab = state.sortModeByTab,
                    customLists = state.customLists,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val (books, editions) = withContext(dependencies.defaultDispatcher) {
                    if (snapshot.gridLayout.isGrid()) {
                        snapshot.toBookCards(previous = bookCache) to
                            snapshot.toEditionCards(previous = editionCache)
                    } else {
                        emptyMap<String, CachedTab<BookTabInputs>>() to
                            emptyMap<String, CachedTab<EditionTabInputs>>()
                    }
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

private fun LibraryGridLayout.isGrid(): Boolean = when (this) {
    LibraryGridLayout.GRID_TWO_COLUMNS,
    LibraryGridLayout.GRID_THREE_COLUMNS,
    LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY,
    LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY,
        -> true

    LibraryGridLayout.LIST_COMPACT,
    LibraryGridLayout.LIST_LARGE,
        -> false
}

private fun LibraryGridLayout.isCoverOnly(): Boolean =
    this == LibraryGridLayout.GRID_TWO_COLUMNS_COVER_ONLY ||
        this == LibraryGridLayout.GRID_THREE_COLUMNS_COVER_ONLY

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
            covers = books.map { bookCovers[it.id] },
            marks = books.map { selectionMarks[it.id] },
            overlays = books.map { deadlineCountdownOverlays[it.id] },
            press = press,
            coverOnly = gridLayout.isCoverOnly(),
        )

        put(
            tab.id,
            cachedTab(
                previous = previous[tab.id],
                inputs = inputs,
            ) {
                books.mapIndexed { index, book ->
                    book.id to BookCardUiModel(
                        key = BookCardKey.Book(bookId = book.id),
                        variant = variantFor(
                            cover = inputs.covers[index],
                            title = book.title,
                            authors = book.authors,
                        ),
                        decorations = BookCardDecorations(
                            progress = book.cardProgress(),
                            coverBadge = inputs.overlays[index],
                            selection = inputs.marks[index],
                            pressBehaviour = press,
                        ),
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
            covers = editions.map { editionCovers[it.id] },
            press = press,
            coverOnly = gridLayout.isCoverOnly(),
        )

        put(
            tab.id,
            cachedTab(
                previous = previous[tab.id],
                inputs = inputs,
            ) {
                editions.mapIndexed { index, edition ->
                    edition.id to BookCardUiModel(
                        key = BookCardKey.Edition(editionId = edition.id),
                        variant = variantFor(
                            cover = inputs.covers[index],
                            title = edition.title.orEmpty(),
                            authors = edition.authors,
                        ),
                        decorations = BookCardDecorations(
                            progress = BookCardProgress.Reserved,
                            pressBehaviour = press,
                        ),
                    )
                }
            },
        )
    }
}

private fun BookCardModelsSnapshot.variantFor(
    cover: CoverUiModel?,
    title: String,
    authors: List<Author>,
): BookCardVariant {
    if (gridLayout.isCoverOnly()) return BookCardVariant.CoverOnly(cover = cover)

    val firstAuthor = authors.firstOrNull()?.name?.takeIf { it.isNotBlank() }

    return BookCardVariant.Stacked(
        cover = cover,
        text = BookCardContent(
            title = title,
            byline = firstAuthor?.let { "By $it" },
        ),
    )
}

private fun Book.cardProgress(): BookCardProgress {
    val progress = userBookRead?.progress

    return if (status == BookStatus.Reading && progress != null) {
        BookCardProgress.Value(
            fraction = progress / 100f,
            label = null,
        )
    } else {
        BookCardProgress.Reserved
    }
}
