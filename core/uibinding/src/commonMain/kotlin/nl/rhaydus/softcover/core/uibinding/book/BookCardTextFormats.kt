package nl.rhaydus.softcover.core.uibinding.book

import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.softcover.core.component.bookcard.BookCardEyebrow
import nl.rhaydus.softcover.core.component.bookcard.BookCardLinePart

/** The "By X" byline for [author], or `null` when it is `null` or blank. */
fun byline(author: String?): String? = author
    ?.takeIf { it.isNotBlank() }
    ?.let { "By $it" }

/** A rating part reading "4.5" with the description "Rated 4.5 out of 5". */
fun ratingPart(rating: Double): BookCardLinePart.Rating {
    val label = formatDecimalNumber(
        rating,
        fractionDigits = 1,
    )

    return BookCardLinePart.Rating(
        label = label,
        contentDescription = "Rated $label out of 5",
    )
}

/** The uppercase series eyebrow for [seriesText], or `null` when it is `null` or blank. */
fun seriesEyebrow(seriesText: String?): BookCardEyebrow? = seriesText
    ?.takeIf { it.isNotBlank() }
    ?.let { BookCardEyebrow(text = it.uppercase()) }
