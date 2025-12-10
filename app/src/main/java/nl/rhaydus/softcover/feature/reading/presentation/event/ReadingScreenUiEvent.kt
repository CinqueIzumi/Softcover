package nl.rhaydus.softcover.feature.reading.presentation.event

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.presentation.ProgressSheetTab

sealed class ReadingScreenUiEvent {
    data object Refresh : ReadingScreenUiEvent()

    data object DismissProgressSheet : ReadingScreenUiEvent()
    data object DismissEditionSheet : ReadingScreenUiEvent()

    data class OnUpdatePageProgressClick(val newPage: String) : ReadingScreenUiEvent()
    data class OnUpdatePercentageProgressClick(val newProgress: String) : ReadingScreenUiEvent()

    data class OnShowProgressSheetClick(val book: BookWithProgress) : ReadingScreenUiEvent()
    data class OnShowEditionSheetClick(val book: BookWithProgress) : ReadingScreenUiEvent()

    data class OnProgressTabClick(val newProgressSheetTab: ProgressSheetTab) : ReadingScreenUiEvent()

    data class OnMarkBookAsReadClick(val book: BookWithProgress) : ReadingScreenUiEvent()
    data class OnNewEditionSaveClick(val edition: BookEdition) : ReadingScreenUiEvent()
}