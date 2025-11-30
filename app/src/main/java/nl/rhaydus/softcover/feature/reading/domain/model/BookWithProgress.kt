package nl.rhaydus.softcover.feature.reading.domain.model

import nl.rhaydus.softcover.core.domain.model.Book

data class BookWithProgress(
    val book: Book,
    val currentPage: Int,
    val progress: Float,
    val editionId: Int,
    val userProgressId: Int,
    val startedAt: String?,
    val finishedAt: String?,
)