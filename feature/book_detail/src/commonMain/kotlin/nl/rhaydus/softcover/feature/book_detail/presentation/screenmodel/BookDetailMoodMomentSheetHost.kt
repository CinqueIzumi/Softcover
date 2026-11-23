package nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel

import nl.rhaydus.softcover.core.progresssheet.presentation.mood.MoodMomentSheetHost
import nl.rhaydus.softcover.core.progresssheet.presentation.mood.MoodMomentSheetState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

internal object BookDetailMoodMomentSheetHost : MoodMomentSheetHost<BookDetailUiState> {
    override fun momentSheetOf(state: BookDetailUiState): MoodMomentSheetState = state.moodMomentSheet

    override fun withMomentSheet(
        state: BookDetailUiState,
        sheet: MoodMomentSheetState,
    ): BookDetailUiState = state.copy(moodMomentSheet = sheet)
}
