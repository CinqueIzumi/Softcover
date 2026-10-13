package nl.rhaydus.softcover.core.uibinding.progress

import nl.rhaydus.common.secondsToHm
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBookRead

/** Where a reader stands in a book as text: [position] ("p. 5 of 300") and what [remaining] is. */
data class BookPositionText(
    val position: String,
    val remaining: String,
)

/** The page count the book's progress is measured against: the current edition's, else the default edition's. */
fun Book.resolvedTotalPages(): Int = currentEdition?.pages ?: defaultEdition?.pages ?: 0

/**
 * The book's position and what is left, in time for an audiobook and pages otherwise; null without a
 * read or a current edition. What is left never goes below zero, so a position past the end reads
 * "0 pages to go".
 */
fun Book.toPositionText(): BookPositionText? {
    val read = userBookRead ?: return null
    val edition = currentEdition ?: return null

    return read.toPositionText(
        edition = edition,
        totalPages = resolvedTotalPages(),
    )
}

/**
 * This read's position in [edition], in time for an audiobook and in pages against [totalPages]
 * otherwise. For a caller that resolves its own edition rather than using [Book.currentEdition].
 */
fun UserBookRead.toPositionText(
    edition: BookEdition,
    totalPages: Int,
): BookPositionText {
    if (edition.isAudiobook) {
        val totalSeconds = edition.audioSeconds ?: 0
        val elapsedSeconds = currentSeconds ?: 0

        return BookPositionText(
            position = "${secondsToHm(elapsedSeconds)} of ${secondsToHm(totalSeconds)}",
            remaining = "${secondsToHm((totalSeconds - elapsedSeconds).coerceAtLeast(0))} left",
        )
    }

    val readPages = currentPage ?: 0

    return BookPositionText(
        position = "p. $readPages of $totalPages",
        remaining = "${(totalPages - readPages).coerceAtLeast(0)} pages to go",
    )
}
