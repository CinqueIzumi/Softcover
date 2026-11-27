package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.control.ToggleEvent
import nl.rhaydus.softcover.core.component.control.ToggleRow
import nl.rhaydus.softcover.feature.settings.presentation.action.OnAskForMoodToggledAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

@Composable
internal fun MoodPromptRow(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    ToggleRow(
        model = state.moodPromptToggleRow,
        onEvent = { event ->
            when (event) {
                is ToggleEvent.Toggled -> runAction(OnAskForMoodToggledAction(newValue = event.checked))
            }
        },
    )
}
