package nl.rhaydus.softcover.feature.reading.presentation.collector

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.toad.ActionScope

internal class SessionStartAvailabilityCollector : ReadingCollector {
    override suspend fun onLaunch(
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
        dependencies: ReadingScreenDependencies,
    ) {
        val controller = dependencies.activeSessionController

        combine(controller.sessionLoaded, controller.activeSession) { loaded, active ->
            loaded && active == null
        }
            .distinctUntilChanged()
            .collect { canStart ->
                scope.setState { it.copy(canStartSession = canStart) }
            }
    }
}
