package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroup
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnManageShelvesRequestedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnShelvesSheetTabPickedAction
import nl.rhaydus.softcover.feature.library.presentation.state.ShelvesSheetUiModel

/**
 * Mobile's only shelf switcher, opened from the masthead's shelf title; desktop keeps its permanent
 * sidebar instead.
 */
@Composable
internal fun LibraryShelvesSheet(
    model: ShelvesSheetUiModel,
    runAction: (LibraryAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    SheetScaffold(
        model = model.scaffold,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.HeaderActionClicked -> runAction(OnManageShelvesRequestedAction())

                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked -> Unit
            }
        },
    ) {
        SheetRowGroup(
            model = model.group,
            onEvent = { event ->
                when (event) {
                    is SheetRowEvent.Selected -> runAction(OnShelvesSheetTabPickedAction(tabId = event.key))
                }
            },
        )
    }
}
