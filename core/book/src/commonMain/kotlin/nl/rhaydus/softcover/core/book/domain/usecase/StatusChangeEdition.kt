package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book

/**
 * The edition a status change writes. A shelved book keeps its stored edition and [requestedEditionId]
 * is ignored; an off-shelf book takes the requested edition, else the one its cover shows.
 */
internal fun Book.statusChangeEditionId(requestedEditionId: Int?): Int? {
    val shelvedBook = userBook
    return when {
        shelvedBook != null -> shelvedBook.editionId
        else -> requestedEditionId ?: currentEdition?.id
    }
}
