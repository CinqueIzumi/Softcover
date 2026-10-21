package nl.rhaydus.softcover.showcase.live.dialog.action

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.showcase.live.dialog.event.DialogLiveEvent
import nl.rhaydus.softcover.showcase.live.dialog.mapper.datePickerTrigger
import nl.rhaydus.softcover.showcase.live.dialog.screenmodel.DialogLiveDependencies
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.dialog.state.DialogLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnDatePickerConfirmedAction(val date: LocalDate) : DialogLiveAction {
    override suspend fun execute(
        dependencies: DialogLiveDependencies,
        scope: ActionScope<DialogLiveUiState, DialogLiveEvent, DialogLiveLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                pickedDate = date,
                trigger = datePickerTrigger(pickedDate = date),
                datePicker = null,
            )
        }
    }
}
