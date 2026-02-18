package nl.rhaydus.softcover.feature.book_detail.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.UiState
import nl.rhaydus.softcover.feature.reading.presentation.enums.ProgressSheetTab
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle

data class BookDetailUiState(
    val loading: Boolean = true,
    val book: Book? = null,
    val fabMenuExpanded: Boolean = false,
    val showEditEditionSheet: Boolean = false,
    val showUpdateProgressSheet: Boolean = false,
    val selectedProgressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,
    val dateStyle: DateStyle = DateStyle.DAY_MONTH_YEAR,
) : UiState