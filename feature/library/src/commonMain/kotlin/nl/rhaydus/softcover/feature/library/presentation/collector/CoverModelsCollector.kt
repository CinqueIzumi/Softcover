package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.designsystem.presentation.transition.bookCoverTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.uibinding.cover.toCoverUiModel
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps every book and edition collected across the Library's shelves to a [CoverUiModel] off the
 * composition (`component-contract.md` § 7.2 R9), keyed by id — [LibraryUiState.bookCovers] and
 * [LibraryUiState.editionCovers] are looked up by id at the render's `itemsIndexed` call site
 * rather than paired index-for-index with `renderIds`. `renderIds` is a `MutableStateList` the
 * reorder library permutes live during a drag; an id-keyed map cannot desync from a list being
 * mutated mid-gesture the way a parallel, index-aligned list of covers could.
 *
 * Deriving off [ActionScope.state] rather than patched into each writer: [LibraryUiState.booksByTab]
 * is written by both `AllBooksCollector` and `BooksByStatusCollector`, and
 * [LibraryUiState.editionsByTab] by `BookListsCollector`. A derived field patched into each of those
 * writers instead would be one forgotten `copy` from a stale cover.
 *
 * Deliberately **not** folded into [DisplayListsCollector]: that one recomputes on every search
 * keystroke, sort change, and filter change — its inputs carry `searchQuery`, `filtersByTab`, and
 * `sortModeByTab`/`sortDirectionByTab`. A cover depends on none of those, and folding this in would
 * re-map every collected cover on every keystroke instead of only when the underlying books/editions
 * actually change. The mapping work runs on [LibraryDependencies.defaultDispatcher] because scanning
 * every book across every shelf is exactly the main-thread cost [ChooseListsSnapshot]'s KDoc
 * apologises for paying elsewhere.
 */
internal class CoverModelsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                CoverModelsSnapshot(
                    booksByTab = state.booksByTab,
                    editionsByTab = state.editionsByTab,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val (bookCovers, editionCovers) = withContext(dependencies.defaultDispatcher) {
                    val bookCovers = snapshot.booksByTab.values.asSequence()
                        .flatten()
                        .associateBy { it.id }
                        .mapValues { (_, book) -> book.toShelfBookCover() }

                    val editionCovers = snapshot.editionsByTab.values.asSequence()
                        .flatten()
                        .associateBy { it.id }
                        .mapValues { (_, edition) -> edition.toShelfEditionCover() }

                    bookCovers to editionCovers
                }

                scope.setState {
                    it.copy(
                        bookCovers = bookCovers,
                        editionCovers = editionCovers,
                    )
                }
            }
    }
}

private fun Book.toShelfBookCover(): CoverUiModel = toCoverUiModel(
    variant = CoverVariant.LibraryShelfItem,
    sharedTransitionKey = bookCoverTransitionKey(
        editionId = currentEdition?.id,
        bookId = id,
    ),
)

private fun BookEdition.toShelfEditionCover(): CoverUiModel = toCoverUiModel(
    defaultEdition = this,
    coverlessTitle = title.orEmpty(),
    variant = CoverVariant.LibraryShelfItem,
    sharedTransitionKey = bookCoverTransitionKey(
        editionId = id,
        bookId = bookId,
        surface = "edition-$id",
    ),
)
