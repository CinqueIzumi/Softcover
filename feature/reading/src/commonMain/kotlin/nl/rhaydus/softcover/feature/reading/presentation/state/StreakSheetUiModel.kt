package nl.rhaydus.softcover.feature.reading.presentation.state

import kotlinx.collections.immutable.ImmutableList
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel

internal data class StreakSheetUiModel(
    val scaffold: SheetScaffoldUiModel,

    /** Most recent first. */
    val rows: ImmutableList<StreakDayRowUiModel>,
)
