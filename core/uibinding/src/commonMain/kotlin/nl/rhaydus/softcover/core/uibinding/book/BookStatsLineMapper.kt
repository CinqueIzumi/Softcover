package nl.rhaydus.softcover.core.uibinding.book

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart
import nl.rhaydus.softcover.core.component.transition.BookTransitionKey

/**
 * The "year · N readers · rating" line under a book's byline. A part is left out when its source is
 * `null`; a [releaseYear] of -1 and a [rating] of 0.0 are Hardcover's "unknown" and count as `null`.
 * Returns `null` when no part remains.
 */
fun bookStatsLine(
    releaseYear: Int?,
    usersCount: Int?,
    rating: Double?,
    ratingTransitionKey: BookTransitionKey? = null,
): ImmutableList<BookCardLinePart>? {
    val parts = listOfNotNull(
        releaseYear?.takeIf { it != -1 }?.let { BookCardLinePart.Text(text = it.toString()) },
        usersCount?.let { BookCardLinePart.Text(text = "${formatGroupedNumber(it)} readers") },
        rating?.takeIf { it != 0.0 }?.let {
            ratingPart(
                rating = it,
                transitionKey = ratingTransitionKey,
            )
        },
    )

    return if (parts.isEmpty()) null else parts.toImmutableList()
}
