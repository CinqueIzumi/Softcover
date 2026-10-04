package nl.rhaydus.softcover.feature.session.presentation.state

import androidx.compose.ui.text.input.TextFieldValue
import nl.rhaydus.toad.UiState

internal data class FocusModeUiState(
    /** The typed page, selection included; seeded once from the stored page by `FocusPageEditorCollector`. */
    val pageDraft: TextFieldValue = TextFieldValue(),
    /** Null until the first active session has been mapped. */
    val pageEditor: FocusPageEditorUiModel? = null,
) : UiState
