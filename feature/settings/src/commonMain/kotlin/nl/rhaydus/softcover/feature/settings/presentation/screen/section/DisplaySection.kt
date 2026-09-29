package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import nl.rhaydus.softcover.core.component.header.SectionHeader
import nl.rhaydus.softcover.feature.settings.presentation.action.SettingsAction
import nl.rhaydus.softcover.feature.settings.presentation.mapper.displayToggleActionFor
import nl.rhaydus.softcover.feature.settings.presentation.state.SettingsScreenUiState

/**
 * The remaining appearance switches, read straight off [SettingsScreenUiState.displayToggleRows] (built
 * off the composition, `component-contract.md` R9) rather than assembled here. Dynamic colour is
 * deliberately *not* here — it belongs to [SpineColourSection], whose tiles it recolours.
 */
@Composable
internal fun DisplaySection(
    state: SettingsScreenUiState,
    runAction: (SettingsAction) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionHeader(model = state.displaySectionHeader)

        Spacer(modifier = Modifier.height(20.dp))

        SettingsRowGroup {
            state.displayToggleRows.forEach { displayToggleRow ->
                row {
                    SettingsToggleRow(
                        label = displayToggleRow.label,
                        gloss = displayToggleRow.gloss,
                        checked = displayToggleRow.checked,
                        onCheckedChange = {
                            runAction(
                                displayToggleActionFor(
                                    key = displayToggleRow.key,
                                    newValue = it,
                                ),
                            )
                        },
                    )
                }
            }
        }
    }
}
