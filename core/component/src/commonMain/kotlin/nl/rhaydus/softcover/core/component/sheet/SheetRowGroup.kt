package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun <K> SheetRowGroup(
    model: SheetRowGroupUiModel<K>,
    onEvent: (SheetRowEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.selectableGroup()) {
        model.rows.forEach { row ->
            SheetRow(
                model = row,
                onEvent = onEvent,
            )
        }
    }
}
