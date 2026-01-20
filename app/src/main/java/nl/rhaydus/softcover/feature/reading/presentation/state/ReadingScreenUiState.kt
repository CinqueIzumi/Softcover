package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.presentation.enum.ProgressSheetTab

data class ReadingScreenUiState(
    val books: List<BookWithProgress> = emptyList(),
    val isLoading: Boolean = true,
    val bookToUpdate: BookWithProgress? = null,
    val progressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,
    val showProgressSheet: Boolean = false,
    val showEditionSheet: Boolean = false,
): UiState