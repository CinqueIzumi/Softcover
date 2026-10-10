package nl.rhaydus.softcover.feature.settings.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal fun discardSheetScaffold(saving: Boolean = false): SheetScaffoldUiModel =
    SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Unsaved changes",
                headline = "Save your tabs?",
                description = "Leave now and your tab changes are lost.",
            ),
        ),
        footer = SheetFooterUiModel.Stacked(
            primary = SheetFooterButton(
                label = "Save",
                dismissesSheet = false,
                running = saving,
            ),
            secondary = SheetFooterButton(
                label = "Discard",
                dismissesSheet = true,
            ),
        ),
    )
