package nl.rhaydus.softcover.feature.widget.presentation.state

import kotlinx.datetime.LocalDateTime
import nl.rhaydus.softcover.core.component.progress.ProgressSheetTab
import nl.rhaydus.softcover.core.component.progress.ProgressSheetUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.uibinding.progress.ProgressTimeDraft
import nl.rhaydus.toad.UiState

internal data class UpdateProgressUiState(
    val themeConfiguration: ThemeConfiguration = ThemeConfiguration(),
    val bookToUpdate: Book? = null,
    val progressSheetTab: ProgressSheetTab = ProgressSheetTab.PAGE,

    /** The reader's uncommitted time entry; `null` seeds the sheet's fields from the book. */
    val progressTimeDraft: ProgressTimeDraft? = null,

    /** The reader's backdate for the next logged action; `null` is "just now". */
    val readAt: LocalDateTime? = null,

    val progressSheet: SheetSession<ProgressSheetUiModel, Nothing> = SheetSession.Closed,
) : UiState
