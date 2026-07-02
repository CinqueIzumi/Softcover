package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList

/**
 * Snapshot of the per-list derivations produced inside [BookListsCollector]'s `combine`. Held as
 * a single value so the downstream `collectLatest` can read everything back atomically when it
 * writes to state — the previous `Triple` ran out of slots once `addedAtByTab` joined the picture.
 */
internal data class CollectedLists(
    val editionsPerList: Map<String, List<BookEdition>>,
    val addedAtPerList: Map<String, Map<Int, String?>>,
    val booksById: Map<Int, Book>,
    val allLists: List<BookList>,
)
