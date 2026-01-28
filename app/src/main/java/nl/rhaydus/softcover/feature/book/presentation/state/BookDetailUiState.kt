package nl.rhaydus.softcover.feature.book.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.reading.presentation.enums.ProgressSheetTab

data class BookDetailUiState(
    val loading: Boolean = true,
    val book: Book? = null,
    val fabMenuExpanded: Boolean = false,
    val showEditEditionSheet: Boolean = false,
    val showUpdateProgressSheet: Boolean = false,
    val selectedProgressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,
) : UiState