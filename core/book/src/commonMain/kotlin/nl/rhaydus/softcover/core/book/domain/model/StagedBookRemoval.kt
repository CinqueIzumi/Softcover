package nl.rhaydus.softcover.core.book.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

/**
 * A library removal applied to the local cache but not yet sent to Hardcover. [books] leaves out any
 * book an earlier removal still holds staged, so it can be shorter than the request. [snapshots] holds
 * each staged book as cached before removal, for an Undo or a failed commit to restore.
 */
class StagedBookRemoval(
    val books: List<Book>,
    val snapshots: List<Book>,
)
