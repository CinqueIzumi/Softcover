package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.core.designsystem.presentation.theme.editorialTypography
import nl.rhaydus.softcover.feature.settings.presentation.action.OnUiScaleSelectedAction
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

@Composable
internal fun UiScaleSection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = state.uiScaleSectionHeader)

        Spacer(modifier = Modifier.height(20.dp))

        SettingsRowGroup(selectableGroup = true) {
            state.uiScaleRows.forEach { uiScaleRow ->
                row {
                    SettingsSelectableRow(
                        label = uiScaleRow.label,
                        example = uiScaleRow.example,
                        isSelected = uiScaleRow.selected,
                        onClick = { runAction(OnUiScaleSelectedAction(scale = uiScaleRow.key)) },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Applies immediately, across the whole app.",
            style = MaterialTheme.editorialTypography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}
