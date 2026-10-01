package nl.rhaydus.softcover.feature.library.presentation.component

import androidx.compose.runtime.Composable
import nl.rhaydus.designsystem.component.LocalModalSheetDismiss
import nl.rhaydus.softcover.core.component.sheet.SheetRowEvent
import nl.rhaydus.softcover.core.component.sheet.SheetRowGroup
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnTabSelectedAction
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * Mobile's only shelf switcher, opened from the masthead's shelf title; desktop keeps its permanent
 * sidebar instead.
 */
@Composable
internal fun LibraryShelvesSheet(
    state: LibraryUiState,
    runAction: (LibraryAction) -> Unit,
    onManageClick: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    SheetScaffold(
        model = state.shelvesSheet,
        onEvent = { event ->
            when (event) {
                SheetScaffoldEvent.HeaderActionClicked -> onManageClick()

                SheetScaffoldEvent.Dismissed -> onDismissRequest()

                SheetScaffoldEvent.TopActionClicked,
                SheetScaffoldEvent.PrimaryClicked,
                SheetScaffoldEvent.SecondaryClicked,
                -> Unit
            }
        },
    ) {
        val dismiss = LocalModalSheetDismiss.current

        SheetRowGroup(
            model = state.shelvesSheetGroup,
            onEvent = { event ->
                when (event) {
                    is SheetRowEvent.Selected -> {
                        runAction(OnTabSelectedAction(tabId = event.key))

                        dismiss()
                    }
                }
            },
        )
    }
}
