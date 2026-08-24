package nl.rhaydus.softcover.core.designsystem.presentation.component

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import nl.rhaydus.common.currentLocalDate
import nl.rhaydus.softcover.core.designsystem.presentation.util.toPickerLocalDate
import nl.rhaydus.softcover.core.designsystem.presentation.util.toPickerMillis

/**
 * The shared calendar dialog. It speaks [LocalDate] at both ends and owns the Material 3 UTC-millis
 * contract internally (see `PickerDates.kt`), so a caller never converts an epoch timestamp itself and
 * cannot reintroduce the timezone off-by-one that shifted a picked day.
 *
 * [initialDate] is the day the calendar opens on and highlights; today is used when it is null.
 * [onConfirm] fires with the tapped day, [onDismiss] with a cancel — including the degenerate case
 * where the picker somehow reports no selection at all.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoftcoverDatePickerDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val initialMillis = remember(initialDate) {
        (initialDate ?: currentLocalDate()).toPickerMillis()
    }

    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = pickerState.selectedDateMillis
                    if (millis != null) {
                        onConfirm(millis.toPickerLocalDate())
                    } else {
                        onDismiss()
                    }
                },
            ) {
                Text(text = "Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    ) {
        DatePicker(state = pickerState)
    }
}
