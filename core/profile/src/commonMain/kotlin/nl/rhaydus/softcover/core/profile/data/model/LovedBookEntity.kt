package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.LovedBook

@Serializable
internal data class LovedBookEntity(
    val bookId: Int? = null,
    val coverUrl: String = "",
    val title: String = "",
    val author: String = "",
    val ratingStars: Double = 0.0,
    val monthLabel: String = "",
)

/** `null` for a cache entry written before the id was stored; it can open nothing, so it is dropped. */
internal fun LovedBookEntity.toModel(): LovedBook? = bookId?.let {
    LovedBook(
        bookId = it,
        coverUrl = coverUrl,
        title = title,
        author = author,
        ratingStars = ratingStars,
        monthLabel = monthLabel,
    )
}

internal fun LovedBook.toEntity(): LovedBookEntity = LovedBookEntity(
    bookId = bookId,
    coverUrl = coverUrl,
    title = title,
    author = author,
    ratingStars = ratingStars,
    monthLabel = monthLabel,
)
