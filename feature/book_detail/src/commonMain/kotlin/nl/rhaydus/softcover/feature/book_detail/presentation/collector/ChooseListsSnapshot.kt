package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import nl.rhaydus.softcover.core.domain.model.BookList

/** The state fields the choose-lists sheet's UI model is derived from. */
internal data class ChooseListsSnapshot(
    val bookId: Int?,
    val bookTitle: String?,
    val userLists: List<BookList>,
    val listsBeingMutated: Set<Int>,
)
