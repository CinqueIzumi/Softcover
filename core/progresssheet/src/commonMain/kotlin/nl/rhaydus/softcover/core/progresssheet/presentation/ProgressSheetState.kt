package nl.rhaydus.softcover.core.progresssheet.presentation

import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft

data class ProgressSheetState(
    val requested: Boolean = false,
    val tab: ProgressSheetTab = ProgressSheetTab.PAGE,

    /** The reader's uncommitted time entry; `null` seeds the sheet's fields from the book. */
    val timeDraft: ProgressTimeDraft? = null,

    /** The reader's backdate for the next logged action; `null` is "just now". */
    val readAt: LocalDateTime? = null,

    val session: SheetSession<ProgressSheetUiModel, Nothing> = SheetSession.Closed,
) {
    fun opened(): ProgressSheetState = copy(
        requested = true,
        readAt = null,
    )
}
