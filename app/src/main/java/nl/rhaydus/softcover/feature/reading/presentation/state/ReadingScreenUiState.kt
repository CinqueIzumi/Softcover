package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress

data class ReadingScreenUiState(
    val books: List<BookWithProgress> = emptyList(),
    val isLoading: Boolean = true,
)