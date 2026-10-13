package nl.rhaydus.softcover.feature.session.presentation.screenmodel

import cafe.adriel.voyager.core.model.screenModelScope
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.presentation.session.ActiveSessionController
import nl.rhaydus.softcover.feature.session.presentation.action.FocusModeAction
import nl.rhaydus.softcover.feature.session.presentation.collector.FocusModeCollector
import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.ToadScreenModel

internal class FocusModeScreenModel(
    activeSessionController: ActiveSessionController,
    dispatchers: AppDispatchers,
    flows: List<FocusModeCollector>,
) : ToadScreenModel<FocusModeUiState, FocusModeEvent, FocusModeDependencies, FocusModeCollector, LocalFocusModeVariables>(
    initializers = flows,
    initialState = FocusModeUiState(),
    initialLocalVariables = LocalFocusModeVariables(),
) {
    override val dependencies: FocusModeDependencies = FocusModeDependencies(
        activeSessionController = activeSessionController,
        coroutineScope = screenModelScope,
        mainDispatcher = dispatchers.main,
    )

    init {
        startInitializers()
    }

    fun runAction(action: FocusModeAction) = dispatch(action)
}
