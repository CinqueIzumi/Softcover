package nl.rhaydus.softcover.core.domain.model

/** What saving a quote at a position would do to the reader's progress. */
data class QuoteProgressAdvance(
    val newPage: Int?,
    val newSeconds: Int?,
    val finishesBook: Boolean,
)

private const val FULL_PERCENT = 100.0

/** `null` when the quote would not move progress forward, or the book has no read or edition to move. */
fun Book.progressAdvanceFor(position: QuotePosition): QuoteProgressAdvance? {
    val read: UserBookRead = currentRead ?: return null
    val edition: BookEdition = currentEdition ?: return null

    return if (edition.isAudiobook) {
        audioAdvance(
            position = position,
            edition = edition,
            read = read,
        )
    } else {
        pagedAdvance(
            position = position,
            edition = edition,
            read = read,
        )
    }
}

private fun audioAdvance(
    position: QuotePosition,
    edition: BookEdition,
    read: UserBookRead,
): QuoteProgressAdvance? {
    val target: Int = when (position) {
        is QuotePosition.Seconds -> position.seconds

        is QuotePosition.Percent -> {
            val total: Int = edition.audioSeconds?.takeIf { it > 0 } ?: return null

            (position.percent / FULL_PERCENT * total).toInt()
        }

        is QuotePosition.Pages -> return null
    }

    if (target <= (read.currentSeconds ?: 0)) return null

    return QuoteProgressAdvance(
        newPage = null,
        newSeconds = target,
        finishesBook = edition.audioSeconds?.let { target >= it } == true,
    )
}

private fun Book.pagedAdvance(
    position: QuotePosition,
    edition: BookEdition,
    read: UserBookRead,
): QuoteProgressAdvance? {
    val target: Int = when (position) {
        is QuotePosition.Pages -> position.page

        is QuotePosition.Percent -> {
            val total: Int = (edition.pages ?: defaultEdition?.pages)?.takeIf { it > 0 } ?: return null

            (position.percent / FULL_PERCENT * total).toInt()
        }

        is QuotePosition.Seconds -> return null
    }

    if (target <= (read.currentPage ?: 0)) return null

    return QuoteProgressAdvance(
        newPage = target,
        newSeconds = null,
        finishesBook = target == edition.pages,
    )
}
