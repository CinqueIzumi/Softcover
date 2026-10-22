package nl.rhaydus.softcover.core.lists.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

data class HiddenRecommendation(
    val bookId: Int,
    val title: String?,
    val coverUrl: String?,
    val authorText: String?,
)

fun Book.toHiddenRecommendation(): HiddenRecommendation = HiddenRecommendation(
    bookId = id,
    title = title,
    coverUrl = coverUrl,
    authorText = authorString.takeIf { it.isNotBlank() },
)
