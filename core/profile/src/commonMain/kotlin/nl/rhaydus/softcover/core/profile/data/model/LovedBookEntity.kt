package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook

@Serializable
internal data class LovedBookEntity(
    val coverUrl: String = "",
    val title: String = "",
    val author: String = "",
    val ratingStars: Double = 0.0,
    val monthLabel: String = "",
)

internal fun LovedBookEntity.toModel(): LovedBook = LovedBook(
    coverUrl = coverUrl,
    title = title,
    author = author,
    ratingStars = ratingStars,
    monthLabel = monthLabel,
)

internal fun LovedBook.toEntity(): LovedBookEntity = LovedBookEntity(
    coverUrl = coverUrl,
    title = title,
    author = author,
    ratingStars = ratingStars,
    monthLabel = monthLabel,
)
