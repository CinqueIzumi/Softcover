package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import nl.rhaydus.softcover.core.progresssheet.presentation.TestEvent
import nl.rhaydus.softcover.core.progresssheet.presentation.TestLocalVariables
import nl.rhaydus.toad.ActionScope
import nl.rhaydus.toad.UiState

internal data class MoodTestState(
    val moment: MoodMomentSheetState = MoodMomentSheetState(),
) : UiState

internal typealias MoodScope = ActionScope<MoodTestState, TestEvent, TestLocalVariables>

internal object MoodTestHost : MoodMomentSheetHost<MoodTestState> {
    override fun momentSheetOf(state: MoodTestState): MoodMomentSheetState = state.moment

    override fun withMomentSheet(
        state: MoodTestState,
        sheet: MoodMomentSheetState,
    ): MoodTestState = state.copy(moment = sheet)
}
