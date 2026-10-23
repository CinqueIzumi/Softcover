package nl.rhaydus.softcover.showcase.live.snackbar.screen

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.overlaypage.screen.OverlayPage
import nl.rhaydus.softcover.showcase.live.overlaypage.screen.OverlayTriggerRow
import nl.rhaydus.softcover.showcase.live.snackbar.action.OnActionSnackbarClickedAction
import nl.rhaydus.softcover.showcase.live.snackbar.action.OnPlainSnackbarClickedAction
import nl.rhaydus.softcover.showcase.live.snackbar.action.OnSnackbarSheetDismissedAction
import nl.rhaydus.softcover.showcase.live.snackbar.action.OnSnackbarSheetOpenedAction
import nl.rhaydus.softcover.showcase.live.snackbar.action.OnSnackbarSheetPostClickedAction
import nl.rhaydus.softcover.showcase.live.snackbar.event.SnackbarLiveEvent
import nl.rhaydus.softcover.showcase.live.snackbar.screenmodel.SnackbarLiveDependencies
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.snackbar.state.SnackbarLiveUiState
import nl.rhaydus.toad.UiAction

@Composable
internal fun SnackbarLiveLayout(
    state: SnackbarLiveUiState,
    runAction: (UiAction<SnackbarLiveDependencies, SnackbarLiveUiState, SnackbarLiveEvent, SnackbarLiveLocalVariables>) -> Unit,
) {
    OverlayPage(
        topBar = state.topBar,
        onBackClick = { runAction(OnBackClickedAction(backEvent = SnackbarLiveEvent.NavigateBack)) },
        onAppearanceClick = { runAction(OnAppearanceClickedAction()) },
    ) {
        OverlayTriggerRow(
            model = state.plainTrigger,
            onClick = { runAction(OnPlainSnackbarClickedAction()) },
        )

        OverlayTriggerRow(
            model = state.actionTrigger,
            onClick = { runAction(OnActionSnackbarClickedAction()) },
        )

        OverlayTriggerRow(
            model = state.sheetTrigger,
            onClick = { runAction(OnSnackbarSheetOpenedAction()) },
        )
    }

    state.sheet.modelOrNull?.let { sheet ->
        SheetScaffold(
            model = sheet,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.PrimaryClicked -> runAction(OnSnackbarSheetPostClickedAction())
                    SheetScaffoldEvent.Dismissed -> runAction(OnSnackbarSheetDismissedAction())
                    SheetScaffoldEvent.SecondaryClicked,
                    SheetScaffoldEvent.HeaderActionClicked,
                    -> Unit
                }
            },
        ) {}
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
