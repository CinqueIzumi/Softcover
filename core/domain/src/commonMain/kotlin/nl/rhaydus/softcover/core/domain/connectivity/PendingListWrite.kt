package nl.rhaydus.softcover.core.domain.connectivity

import nl.rhaydus.softcover.core.domain.model.PrivacySetting

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
    /**
     * The privacy the user chose when queuing a [PendingListWriteKind.CREATE_LIST] write. Only that
     * kind ever sets it; every other kind leaves it `null` so a queued create never silently drops
     * the reader's choice on replay (see [nl.rhaydus.softcover.core.connectivity.data.sync.ListWriteReplay]).
     */
    val privacy: PrivacySetting? = null,
)
