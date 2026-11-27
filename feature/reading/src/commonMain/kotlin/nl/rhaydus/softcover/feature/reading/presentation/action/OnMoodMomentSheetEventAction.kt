package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.component.feeling.MoodMomentSheetEvent
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingMoodMomentSheetHost
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal data class OnMoodMomentSheetEventAction(val event: MoodMomentSheetEvent) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        dependencies.moodMomentSheetSlice.onEvent(
            event = event,
            host = ReadingMoodMomentSheetHost,
            dependencies = dependencies,
            scope = scope,
        )
    }
}
