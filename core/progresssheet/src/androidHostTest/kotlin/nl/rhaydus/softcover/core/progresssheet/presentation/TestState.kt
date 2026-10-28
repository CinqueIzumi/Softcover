package nl.rhaydus.softcover.core.progresssheet.presentation

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.toad.UiState

internal data class TestState(
    val book: Book? = null,
    val progressSheet: ProgressSheetState = ProgressSheetState(),
) : UiState
