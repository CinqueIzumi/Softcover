package nl.rhaydus.softcover.feature.reading.presentation.event

import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress

sealed class ReadingScreenUiEvent {
    data object Refresh : ReadingScreenUiEvent()
    data object DismissProgressSheet : ReadingScreenUiEvent()

    data class OnSetProgressClick(val book: BookWithProgress) : ReadingScreenUiEvent()
    data class OnUpdateProgressClick(val newPage: String) : ReadingScreenUiEvent()
}