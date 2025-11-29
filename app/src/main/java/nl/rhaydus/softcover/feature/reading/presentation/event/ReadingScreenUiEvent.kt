package nl.rhaydus.softcover.feature.reading.presentation.event
 
sealed class ReadingScreenUiEvent {
    data object Refresh : ReadingScreenUiEvent()
}