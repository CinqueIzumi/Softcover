package nl.rhaydus.softcover.showcase.live.dialog.action

import nl.rhaydus.softcover.showcase.live.dialog.event.DialogLiveEvent
import nl.rhaydus.softcover.showcase.live.dialog.mapper.datePicker
import nl.rhaydus.softcover.showcase.live.dialog.screenmodel.DialogLiveDependencies
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnDatePickerOpenedAction : DialogLiveAction {
    override suspend fun execute(
        dependencies: DialogLiveDependencies,
        scope: ActionScope<DialogLiveUiState, DialogLiveEvent, DialogLiveLocalVariables>,
    ) {
        scope.setState { it.copy(datePicker = datePicker(pickedDate = it.pickedDate)) }
    }
}
