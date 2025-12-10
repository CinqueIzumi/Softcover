package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.presentation.ProgressSheetTab

data class ReadingScreenUiState(
    val books: List<BookWithProgress> = emptyList(),
    val isLoading: Boolean = true,
    val bookToUpdate: BookWithProgress? = null,
    val progressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,
    val showProgressSheet: Boolean = false,
    val showEditionSheet: Boolean = false,
)