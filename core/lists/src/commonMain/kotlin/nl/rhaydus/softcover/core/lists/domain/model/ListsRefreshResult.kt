package nl.rhaydus.softcover.core.lists.domain.model

import nl.rhaydus.softcover.core.domain.model.BookList

/**
 * The outcome of a gated full-list refresh.
 *
 * [serverListIds] is the complete set of list ids the server currently reports (from the cheap
 * signature query), so callers can still drop lists deleted elsewhere. [changedLists] are only the
 * lists whose freshness signature advanced (or that are new), fully fetched with their `list_books`
 * — unchanged lists are omitted so their already-cached contents are left untouched.
 */
data class ListsRefreshResult(
    val serverListIds: Set<Int>,
    val changedLists: List<BookList>,
)
