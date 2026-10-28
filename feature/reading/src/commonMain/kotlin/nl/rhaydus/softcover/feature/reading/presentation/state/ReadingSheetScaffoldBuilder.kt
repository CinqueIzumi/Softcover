package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal fun streakSheetScaffoldFor(
    readDays: Int,
    dayCount: Int,
): SheetScaffoldUiModel {
    val headline = when (readDays) {
        0 -> "No reading logged yet"
        1 -> "You read on 1 of the last $dayCount days"
        else -> "You read on $readDays of the last $dayCount days"
    }

    return SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Last $dayCount days",
                headline = headline,
            ),
        ),
    )
}
