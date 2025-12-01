package nl.rhaydus.softcover.feature.reading.presentation.event

import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.presentation.state.ProgressTab

sealed class ReadingScreenUiEvent {
    data object Refresh : ReadingScreenUiEvent()
    data object DismissProgressSheet : ReadingScreenUiEvent()

    data class OnUpdatePageProgressClick(val newPage: String) : ReadingScreenUiEvent()
    data class OnUpdatePercentageProgressClick(val newProgress: String): ReadingScreenUiEvent()

    data class OnShowProgressSheetClick(val book: BookWithProgress) : ReadingScreenUiEvent()
    data class OnProgressTabClick(val newProgressTab: ProgressTab) : ReadingScreenUiEvent()

    data class OnMarkBookAsReadClick(val book: BookWithProgress) : ReadingScreenUiEvent()
}