package nl.rhaydus.softcover.core.domain.connectivity

data class PendingListWrite(
    val kind: PendingListWriteKind,
    val listId: Int?,
    val listName: String?,
    val bookId: Int?,
    val editionId: Int?,
    val listBookId: Int?,
    val startPosition: Int?,
    val orderedListBookIds: List<Int>?,
    val enqueuedAt: String,
)
