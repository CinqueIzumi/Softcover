package nl.rhaydus.softcover.feature.widget.presentation.state

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.progresssheet.presentation.ProgressSheetState
import nl.rhaydus.toad.UiState

internal data class UpdateProgressUiState(
    val themeConfiguration: ThemeConfiguration = ThemeConfiguration(),
    val bookToUpdate: Book? = null,
    val progressSheet: ProgressSheetState = ProgressSheetState(),
) : UiState
