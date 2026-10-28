package nl.rhaydus.softcover.core.progresssheet.presentation

import nl.rhaydus.softcover.core.domain.model.Book

internal fun pageEntry(
    page: String,
    actionAt: String?,
): ProgressEntry = ProgressEntry(
    newPage = page.toIntOrNull() ?: 0,
    newSeconds = null,
    actionAt = actionAt,
)

/** Null when the book has no current edition to take a fraction of. */
internal fun percentageEntry(
    book: Book,
    percentage: String,
    actionAt: String?,
): ProgressEntry? {
    val fraction = ((percentage.toDoubleOrNull() ?: 0.0) / 100.0).coerceIn(
        0.0,
        1.0,
    )

    val edition = book.currentEdition ?: return null

    return if (edition.isAudiobook) {
        ProgressEntry(
            newPage = null,
            newSeconds = (fraction * (edition.audioSeconds ?: 0)).toInt(),
            actionAt = actionAt,
        )
    } else {
        ProgressEntry(
            newPage = (fraction * (edition.pages ?: book.defaultEdition?.pages ?: 0)).toInt(),
            newSeconds = null,
            actionAt = actionAt,
        )
    }
}

internal fun timeEntry(
    book: Book,
    hours: String,
    minutes: String,
    seconds: String,
    actionAt: String?,
): ProgressEntry {
    val entered = (hours.toIntOrNull() ?: 0) * 3600 +
        (minutes.toIntOrNull() ?: 0) * 60 +
        (seconds.toIntOrNull() ?: 0)

    val total = book.currentEdition?.audioSeconds ?: 0

    val newSeconds = if (total > 0) {
        entered.coerceIn(
            0,
            total,
        )
    } else {
        entered.coerceAtLeast(0)
    }

    return ProgressEntry(
        newPage = null,
        newSeconds = newSeconds,
        actionAt = actionAt,
    )
}
