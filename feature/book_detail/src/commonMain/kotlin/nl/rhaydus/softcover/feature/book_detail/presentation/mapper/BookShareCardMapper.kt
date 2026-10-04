package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.component.verdict.verdictScoreOf
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import kotlin.math.roundToInt

internal fun Book.toBookShareCardUiModel(edition: BookEdition?): BookShareCardUiModel {
    val resolvedEdition = edition ?: defaultEdition

    return BookShareCardUiModel(
        coverUrl = resolvedEdition?.localImagePath
            ?: resolvedEdition?.url
            ?: coverUrl,
        title = title,
        author = authors.firstOrNull()?.name.orEmpty(),
        statsLine = statsLineOf(
            communityRating = rating.takeIf { it > 0.0 },
            userRating = verdictScoreOf(rating = userBook?.rating?.takeIf { it > 0.0 }),
            releaseYear = resolvedEdition?.releaseYear?.takeIf { it != -1 } ?: releaseYear.takeIf { it != -1 },
            pageCount = resolvedEdition?.pages,
        ),
        description = description.takeIf { it.isNotBlank() },
    )
}

private fun statsLineOf(
    communityRating: Double?,
    userRating: Int?,
    releaseYear: Int?,
    pageCount: Int?,
): String? {
    val parts = buildList {
        communityRating?.let {
            val tenths = (it * 10).roundToInt()

            add("★ ${tenths / 10}.${tenths % 10}")
        }
        userRating?.let { add("Your $it/10") }
        releaseYear?.let { add(it.toString()) }
        pageCount?.takeIf { it > 0 }?.let { add("$it pages") }
    }

    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = " · ")
}
