package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDateStyleClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

@Composable
internal fun DateStyleSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = state.dateStyleSectionHeader)

        Spacer(modifier = Modifier.height(20.dp))

        SettingsRowGroup(selectableGroup = true) {
            state.dateStyleRows.forEach { dateStyleRow ->
                row {
                    SettingsSelectableRow(
                        model = dateStyleRow,
                        onClick = { runAction(OnDateStyleClickAction(style = dateStyleRow.key)) },
                    )
                }
            }
        }
    }
}
