package nl.rhaydus.softcover.core.uibinding.book

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.bookcard.BookCardLine
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart

/**
 * The "year · N readers · rating" line under a book's byline. A part is left out when its source is
 * `null`; a [releaseYear] of -1 and a [rating] of 0.0 are Hardcover's "unknown" and count as `null`.
 * Returns `null` when no part remains.
 */
fun bookStatsLine(
    releaseYear: Int?,
    usersCount: Int?,
    rating: Double?,
): BookCardLine? {
    val parts = listOfNotNull(
        releaseYear?.takeIf { it != -1 }?.let { BookCardLinePart.Text(text = it.toString()) },
        usersCount?.let { BookCardLinePart.Text(text = "${formatGroupedNumber(it)} readers") },
        rating?.takeIf { it != 0.0 }?.let {
            BookCardLinePart.Rating(
                label = formatDecimalNumber(
                    it,
                    fractionDigits = 1,
                ),
            )
        },
    )

    return if (parts.isEmpty()) null else BookCardLine(parts = parts.toImmutableList())
}
