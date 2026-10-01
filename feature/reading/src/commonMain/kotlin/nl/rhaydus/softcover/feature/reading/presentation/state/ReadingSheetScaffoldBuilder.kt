package nl.rhaydus.softcover.feature.reading.presentation.state

import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.domain.model.ReadingDayActivity

internal fun streakSheetScaffoldFor(activity: List<ReadingDayActivity>): SheetScaffoldUiModel {
    val readDays = activity.count { it.didRead }

    val headline = when (readDays) {
        0 -> "No reading logged yet"
        1 -> "You read on 1 of the last ${activity.size} days"
        else -> "You read on $readDays of the last ${activity.size} days"
    }

    return SheetScaffoldUiModel(
        header = SheetHeaderUiModel(
            section = SectionHeaderUiModel.Section(
                eyebrow = "Last ${activity.size} days",
                headline = headline,
            ),
        ),
    )
}
