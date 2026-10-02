package nl.rhaydus.softcover.core.component.progress

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.text.SoftcoverText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TabSwitcher(
    activeTab: ProgressSheetTab,
    visibleTabs: List<ProgressSheetTab>,
    onEvent: (ProgressSheetEvent) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        visibleTabs.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = tab == activeTab,
                onClick = { onEvent(ProgressSheetEvent.TabSelected(tab = tab)) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = visibleTabs.size,
                ),
                label = {
                    SoftcoverText(
                        text = tab.tabName,
                        style = MaterialTheme.typography.labelLarge,
                    )
                },
            )
        }
    }
}
