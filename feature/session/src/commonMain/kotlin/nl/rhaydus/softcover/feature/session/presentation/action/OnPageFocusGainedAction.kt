package nl.rhaydus.softcover.feature.session.presentation.action

import androidx.compose.ui.text.TextRange
import nl.rhaydus.softcover.feature.session.presentation.event.FocusModeEvent
import nl.rhaydus.softcover.feature.session.presentation.screenmodel.FocusModeDependencies
import nl.rhaydus.softcover.feature.session.presentation.state.FocusModeUiState
import nl.rhaydus.softcover.feature.session.presentation.state.LocalFocusModeVariables
import nl.rhaydus.toad.ActionScope

internal data object OnPageFocusGainedAction : FocusModeAction {
    override suspend fun execute(
        dependencies: FocusModeDependencies,
        scope: ActionScope<FocusModeUiState, FocusModeEvent, LocalFocusModeVariables>,
    ) {
        scope.setState {
            it.copy(
                pageDraft = it.pageDraft.copy(
                    selection = TextRange(
                        start = 0,
                        end = it.pageDraft.text.length,
                    ),
                ),
            )
        }
    }
}
