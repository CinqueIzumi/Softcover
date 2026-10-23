package nl.rhaydus.softcover.core.lists.data.mapper

import nl.rhaydus.softcover.core.database.model.HiddenRecommendationEntity
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation

internal fun HiddenRecommendationEntity.toModel(): HiddenRecommendation = HiddenRecommendation(
    bookId = bookId,
    title = bookTitle,
    coverUrl = coverUrl,
    authorText = authorText,
)

internal fun HiddenRecommendation.toEntity(): HiddenRecommendationEntity = HiddenRecommendationEntity(
    bookId = bookId,
    bookTitle = title,
    coverUrl = coverUrl,
    authorText = authorText,
)
