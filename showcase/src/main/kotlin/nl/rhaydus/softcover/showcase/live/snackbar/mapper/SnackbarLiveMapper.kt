package nl.rhaydus.softcover.showcase.live.snackbar.mapper

import nl.rhaydus.softcover.core.component.gallery.GalleryFamily
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.showcase.live.overlaypage.model.OverlayTriggerUiModel

internal fun snackbarLiveTopBar(): TopBarUiModel = TopBarUiModel(title = GalleryFamily.SNACKBAR.label)

internal fun plainSnackbarTrigger(): OverlayTriggerUiModel = OverlayTriggerUiModel(
    label = "A plain snackbar",
    description = "One line, no action. It leaves on its own.",
)

internal fun actionSnackbarTrigger(): OverlayTriggerUiModel = OverlayTriggerUiModel(
    label = "A snackbar with an action",
    description = "Take the action and a second snackbar answers it.",
)

internal fun sheetSnackbarTrigger(): OverlayTriggerUiModel = OverlayTriggerUiModel(
    label = "A snackbar over a sheet",
    description = "Opens a sheet. Its button posts a snackbar that has to land above it.",
)

internal fun snackbarSheet(): SheetScaffoldUiModel = SheetScaffoldUiModel(
    header = SheetHeaderUiModel(
        section = SectionHeaderUiModel.Section(
            eyebrow = "Snackbars",
            headline = "Above the sheet",
            description = "Post a snackbar from here and watch which layer it takes.",
        ),
    ),
    footer = SheetFooterUiModel.Stacked(
        primary = SheetFooterButton(label = "Post a snackbar"),
        secondary = null,
    ),
    dismissLabel = "Done",
)
