package nl.rhaydus.softcover.feature.reading.domain.model

import nl.rhaydus.softcover.core.domain.Book

data class BookWithProgress(
    val book: Book,
    val currentPage: Int,
    val progress: Float,
)