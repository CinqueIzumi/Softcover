package nl.rhaydus.softcover.core.component.dialog

import androidx.compose.runtime.Immutable
import kotlinx.datetime.LocalDate

/**
 * Everything [SoftcoverDatePickerDialog] renders: the day the calendar opens on and highlights.
 *
 * [initialDate] is required, so the caller decides the fallback (usually today) where it builds the
 * model rather than the dialog reading the clock during composition.
 */
@Immutable
data class DatePickerUiModel(
    val initialDate: LocalDate,
)
