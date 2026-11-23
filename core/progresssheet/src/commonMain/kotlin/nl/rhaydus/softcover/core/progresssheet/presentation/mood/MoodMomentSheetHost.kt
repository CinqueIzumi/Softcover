package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import nl.rhaydus.toad.UiState

interface MoodMomentSheetHost<S : UiState> {
    fun momentSheetOf(state: S): MoodMomentSheetState

    fun withMomentSheet(
        state: S,
        sheet: MoodMomentSheetState,
    ): S
}
