package nl.rhaydus.softcover.feature.session.presentation.action

import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.ActionScope

internal data object OnPageShakeEndedAction : FocusModeAction {
    override suspend fun execute(
        dependencies: FocusModeDependencies,
        scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>,
    ) {
        scope.setState { it.copy(pageDraftRejected = false) }
    }
}
