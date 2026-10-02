package nl.rhaydus.softcover.core.component.dialog

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import nl.rhaydus.softcover.core.component.text.SoftcoverText

/**
 * The shared calendar dialog for picking a single day — the deadline picker on Book Detail is a
 * consumer. It reports a [kotlinx.datetime.LocalDate] and owns the Material 3 UTC-millis contract
 * internally (see `PickerDates.kt`), so a caller never converts an epoch timestamp itself and cannot
 * reintroduce the timezone off-by-one that shifted a picked day.
 *
 * Confirming with no selection reports [DatePickerEvent.Dismissed], the same as cancelling.
 *
 * Any new "pick a date" surface reaches for this dialog; hand-rolling a `DatePickerDialog` and
 * converting the millis at the call site is the bug it exists to prevent.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftcoverDatePickerDialog(
    model: DatePickerUiModel,
    onEvent: (DatePickerEvent) -> Unit,
) {
    val initialMillis = remember(model.initialDate) { model.initialDate.toPickerMillis() }

    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = { onEvent(DatePickerEvent.Dismissed) },
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        onEvent(DatePickerEvent.Confirmed(date = millis.toPickerLocalDate()))
                    } else {
                        onEvent(DatePickerEvent.Dismissed)
                    }
                },
            ) {
                SoftcoverText(text = model.confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = { onEvent(DatePickerEvent.Dismissed) }) {
                SoftcoverText(text = model.cancelLabel)
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}
