package nl.rhaydus.softcover.core.progresssheet.presentation

import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft

internal data class ProgressSheetSnapshot(
    val book: Book?,
    val tab: ProgressSheetTab,
    val timeDraft: ProgressTimeDraft?,
    val readAt: LocalDateTime?,
)
