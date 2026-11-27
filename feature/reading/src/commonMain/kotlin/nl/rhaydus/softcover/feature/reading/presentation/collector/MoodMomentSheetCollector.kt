package nl.rhaydus.softcover.feature.reading.presentation.collector

import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingMoodMomentSheetHost
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal class MoodMomentSheetCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        dependencies.moodMomentSheetSlice.collect(
            host = ReadingMoodMomentSheetHost,
            scope = scope,
        )
    }
}
