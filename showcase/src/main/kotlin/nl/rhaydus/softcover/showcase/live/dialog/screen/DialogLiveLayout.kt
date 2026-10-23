package nl.rhaydus.softcover.showcase.live.dialog.screen

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.dialog.DatePickerEvent
import nl.rhaydus.softcover.core.component.dialog.SoftcoverDatePickerDialog
import nl.rhaydus.softcover.showcase.appearance.action.OnAppearanceClickedAction
import nl.rhaydus.softcover.showcase.appearance.action.OnBackClickedAction
import nl.rhaydus.softcover.showcase.appearance.screen.AppearanceSheetHost
import nl.rhaydus.softcover.showcase.live.dialog.action.OnDatePickerConfirmedAction
import nl.rhaydus.softcover.showcase.live.dialog.action.OnDatePickerDismissedAction
import nl.rhaydus.softcover.showcase.live.dialog.action.OnDatePickerOpenedAction
import nl.rhaydus.softcover.showcase.live.dialog.event.DialogLiveEvent
import nl.rhaydus.softcover.showcase.live.dialog.screenmodel.DialogLiveDependencies
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveUiState
import nl.rhaydus.softcover.showcase.live.overlaypage.screen.OverlayPage
import nl.rhaydus.softcover.showcase.live.overlaypage.screen.OverlayTriggerRow
import nl.rhaydus.toad.UiAction

@Composable
internal fun DialogLiveLayout(
    state: DialogLiveUiState,
    runAction: (UiAction<DialogLiveDependencies, DialogLiveUiState, DialogLiveEvent, DialogLiveLocalVariables>) -> Unit,
) {
    OverlayPage(
        topBar = state.topBar,
        onBackClick = { runAction(OnBackClickedAction(backEvent = DialogLiveEvent.NavigateBack)) },
        onAppearanceClick = { runAction(OnAppearanceClickedAction()) },
    ) {
        OverlayTriggerRow(
            model = state.trigger,
            onClick = { runAction(OnDatePickerOpenedAction()) },
        )
    }

    state.datePicker?.let { picker ->
        SoftcoverDatePickerDialog(
            model = picker,
            onEvent = { event ->
                when (event) {
                    is DatePickerEvent.Confirmed -> runAction(OnDatePickerConfirmedAction(date = event.date))
                    DatePickerEvent.Dismissed -> runAction(OnDatePickerDismissedAction())
                }
            },
        )
    }

    AppearanceSheetHost(
        appearance = state.appearance,
        runAction = runAction,
    )
}
