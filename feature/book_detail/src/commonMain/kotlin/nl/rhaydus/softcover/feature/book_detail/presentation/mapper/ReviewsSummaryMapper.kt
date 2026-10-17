package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.common.formatDecimalNumber
import nl.rhaydus.common.formatGroupedNumber
import nl.rhaydus.softcover.core.domain.model.Book

/** "4.2 across 1,204 ratings" for the reviews footer, or `null` while [Book] has no ratings. */
internal fun Book.toReviewsSummary(): String? = takeIf { ratingsCount > 0 }?.let {
    val average = formatDecimalNumber(
        value = rating,
        fractionDigits = 1,
    )

    "$average across ${formatGroupedNumber(ratingsCount)} ratings"
}
