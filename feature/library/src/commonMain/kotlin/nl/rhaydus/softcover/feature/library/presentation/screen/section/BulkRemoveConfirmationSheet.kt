package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

@Composable
internal fun BulkRemoveConfirmationSheet(
    model: SheetScaffoldUiModel,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SheetScaffold(
        model = model,
        modifier = modifier,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.PrimaryClicked -> onConfirm()

                SheetScaffoldEvent.Dismissed -> onDismiss()

                SheetScaffoldEvent.SecondaryClicked,
                SheetScaffoldEvent.TopActionClicked,
                SheetScaffoldEvent.HeaderActionClicked,
                -> Unit
            }
        },
        content = {},
    )
}
