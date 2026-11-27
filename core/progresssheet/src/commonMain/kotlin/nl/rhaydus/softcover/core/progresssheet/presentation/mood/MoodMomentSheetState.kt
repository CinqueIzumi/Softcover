package nl.rhaydus.softcover.core.progresssheet.presentation.mood

import nl.rhaydus.softcover.core.component.feeling.MoodMomentSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession

data class MoodMomentSheetState(
    val draft: MoodMomentDraft? = null,
    val session: SheetSession<MoodMomentSheetUiModel, Nothing> = SheetSession.Closed,
)
