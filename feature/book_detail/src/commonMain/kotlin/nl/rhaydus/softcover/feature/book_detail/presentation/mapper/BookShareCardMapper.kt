package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import nl.rhaydus.softcover.core.component.share.BookShareCardUiModel
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition

internal fun Book.toBookShareCardUiModel(edition: BookEdition?): BookShareCardUiModel {
    val resolvedEdition = edition ?: defaultEdition

    return BookShareCardUiModel(
        coverUrl = resolvedEdition?.localImagePath
            ?: resolvedEdition?.url
            ?: coverUrl,
        title = title,
        author = authors.firstOrNull()?.name.orEmpty(),
        communityRating = rating.takeIf { it > 0.0 },
        userRating = userBook?.rating?.toInt(),
        releaseYear = resolvedEdition?.releaseYear?.takeIf { it != -1 } ?: releaseYear.takeIf { it != -1 },
        pageCount = resolvedEdition?.pages,
        description = description.takeIf { it.isNotBlank() },
        quote = null,
    )
}
