package nl.rhaydus.softcover.feature.session.presentation.action

import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.UiAction

internal sealed interface FocusModeAction : UiAction<
        FocusModeDependencies,
        FocusModeUiState,
        FocusModeEvent,
        LocalFocusModeVariables,
        >
