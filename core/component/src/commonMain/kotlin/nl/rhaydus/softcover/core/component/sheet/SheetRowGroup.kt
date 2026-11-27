package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss

@Composable
fun <K> SheetRowGroup(
    model: SheetRowGroupUiModel<K>,
    onEvent: (SheetRowEvent<K>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissSheet = LocalModalSheetDismiss.current

    Column(modifier = modifier.selectableGroup()) {
        model.rows.forEach { row ->
            SheetRow(
                model = row,
                face = model.face,
                onEvent = { event ->
                    onEvent(event)

                    if (model.dismissesSheet) {
                        dismissSheet()
                    }
                },
            )
        }

        if (model.input != null) {
            SheetInputRow(
                model = model.input,
                onEvent = { event -> onEvent(SheetRowEvent.InputChanged(event = event)) },
            )
        }
    }
}
