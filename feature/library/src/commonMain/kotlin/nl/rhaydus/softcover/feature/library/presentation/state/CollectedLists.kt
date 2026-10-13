package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.BookList

/**
 * Snapshot of the per-list derivations produced inside [BookListsCollector]'s `combine`. Held as
 * a single value so the downstream `collectLatest` can read everything back atomically when it
 * writes to state — the previous `Triple` ran out of slots once `addedAtByTab` joined the picture.
 */
internal data class CollectedLists(
    val editionsPerList: ImmutableMap<String, ImmutableList<BookEdition>>,
    val addedAtPerList: ImmutableMap<String, ImmutableMap<Int, String?>>,
    val booksById: ImmutableMap<Int, Book>,
    val allLists: ImmutableList<BookList>,
)
