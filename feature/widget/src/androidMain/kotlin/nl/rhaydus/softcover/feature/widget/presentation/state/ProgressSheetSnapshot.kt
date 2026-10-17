package nl.rhaydus.softcover.feature.widget.presentation.state

import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft

internal data class ProgressSheetSnapshot(
    val bookToUpdate: Book?,
    val selectedTab: ProgressSheetTab,
    val timeDraft: ProgressTimeDraft?,
    val readAt: LocalDateTime?,
)
