package nl.rhaydus.softcover.showcase.appearance.mapper

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal fun appearanceSheet(): SheetScaffoldUiModel = SheetScaffoldUiModel(
    header = SheetHeaderUiModel(
        section = SectionHeaderUiModel.Section(
            eyebrow = "Showcase",
            headline = "Appearance",
            description = "Choose the spine colour and the light the whole showcase is read in.",
        ),
    ),
    dismissLabel = "Done",
)
