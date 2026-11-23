package nl.rhaydus.softcover.feature.reading.presentation.screenmodel

import nl.rhaydus.softcover.core.progresssheet.presentation.mood.MoodMomentSheetHost
import nl.rhaydus.softcover.core.progresssheet.presentation.mood.MoodMomentSheetState
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState

internal object ReadingMoodMomentSheetHost : MoodMomentSheetHost<ReadingScreenUiState> {
    override fun momentSheetOf(state: ReadingScreenUiState): MoodMomentSheetState = state.moodMomentSheet

    override fun withMomentSheet(
        state: ReadingScreenUiState,
        sheet: MoodMomentSheetState,
    ): ReadingScreenUiState = state.copy(moodMomentSheet = sheet)
}
