package nl.rhaydus.softcover.feature.settings.presentation.screen.section

import androidx.compose.runtime.Composable
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.feature.settings.presentation.action.LibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDiscardLibraryVisibilityChangesAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnDismissDiscardSheetAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnLibraryVisibilityBackClickAction
import nl.rhaydus.softcover.feature.settings.presentation.action.OnSaveLibraryVisibilityAction
import nl.rhaydus.softcover.feature.settings.presentation.state.LibraryVisibilitySettingsUiState

@Composable
internal fun LibraryVisibilityDiscardGuard(
    state: LibraryVisibilitySettingsUiState,
    runAction: (LibraryVisibilityAction) -> Unit,
) {
    val backState = rememberNavigationEventState(NavigationEventInfo.None)

    NavigationBackHandler(
        state = backState,
        isBackEnabled = state.isDirty,
        onBackCancelled = {},
        onBackCompleted = { runAction(OnLibraryVisibilityBackClickAction()) },
    )

    if (state.showDiscardSheet) {
        SheetScaffold(
            model = state.discardSheetScaffold,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.PrimaryClicked -> runAction(OnSaveLibraryVisibilityAction(leaveAfterSave = true))

                    SheetScaffoldEvent.SecondaryClicked -> runAction(OnDiscardLibraryVisibilityChangesAction())

                    SheetScaffoldEvent.Dismissed -> runAction(OnDismissDiscardSheetAction())

                    SheetScaffoldEvent.HeaderActionClicked -> Unit
                }
            },
            content = {},
        )
    }
}
