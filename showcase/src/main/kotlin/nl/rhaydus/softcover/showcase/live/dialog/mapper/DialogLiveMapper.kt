package nl.rhaydus.softcover.showcase.live.dialog.mapper

import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.component.dialog.DatePickerUiModel
import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.live.overlaypage.model.OverlayTriggerUiModel

internal fun dialogLiveTopBar(): TopBarUiModel = TopBarUiModel(title = GalleryFamily.DIALOG.label)

internal fun datePickerTrigger(pickedDate: LocalDate?): OverlayTriggerUiModel = OverlayTriggerUiModel(
    label = "Pick a date",
    description = if (pickedDate == null) {
        "No day chosen yet. Tap to open the calendar."
    } else {
        "You picked ${pickedDate.day} ${pickedDate.month.name.lowercase().replaceFirstChar { it.uppercase() }} " +
            "${pickedDate.year}."
    },
)

internal fun datePicker(pickedDate: LocalDate?): DatePickerUiModel = DatePickerUiModel(
    initialDate = pickedDate ?: DatePickerUiModel.previews.getValue("Opens on a day").initialDate,
)
