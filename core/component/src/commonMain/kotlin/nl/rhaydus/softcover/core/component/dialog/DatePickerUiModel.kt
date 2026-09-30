package nl.rhaydus.softcover.core.component.dialog

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [SoftcoverDatePickerDialog] renders: the day the calendar opens on and highlights, and
 * the two button labels.
 *
 * [initialDate] is required, so the caller decides the fallback (usually today) where it builds the
 * model rather than the dialog reading the clock during composition.
 */
@Immutable
data class DatePickerUiModel(
    val initialDate: LocalDate,
    val confirmLabel: String,
    val cancelLabel: String,
) {
    companion object : UiModelPreviews<DatePickerUiModel> {
        override val previews: ImmutableList<DatePickerUiModel> = persistentListOf(
            DatePickerUiModel(
                initialDate = LocalDate(
                    2026,
                    6,
                    15,
                ),
                confirmLabel = "Confirm",
                cancelLabel = "Cancel",
            ),
        )
    }
}
