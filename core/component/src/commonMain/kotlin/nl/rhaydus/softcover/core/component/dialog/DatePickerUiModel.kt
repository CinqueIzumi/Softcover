package nl.rhaydus.softcover.core.component.dialog

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [SoftcoverDatePickerDialog] renders: the day the calendar opens on and highlights.
 *
 * [initialDate] is required, so the caller decides the fallback (usually today) where it builds the
 * model rather than the dialog reading the clock during composition.
 */
@Immutable
data class DatePickerUiModel(
    val initialDate: LocalDate,
) {
    companion object : UiModelPreviews<DatePickerUiModel> {
        override val previews: ImmutableMap<String, DatePickerUiModel> = persistentMapOf(
            "Opens on a day" to DatePickerUiModel(
                initialDate = LocalDate(
                    year = 2026,
                    month = 3,
                    day = 14,
                ),
            ),
        )
    }
}
