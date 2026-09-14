package nl.rhaydus.softcover.core.presentation.session

import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.ReadingSession

/**
 * The currently-running reading session paired with the book it belongs to.
 *
 * @property peekBarCover [book]'s cover, mapped for the session peek bar (`SessionPeekBar`).
 * @property focusCover [book]'s cover, mapped for Focus Mode's shelf (`FocusModeShelf`).
 * Both surfaces read a cover off this controller-published model with no `UiState` of their own, so
 * the mapping lives here — the single writer, off the composition (`component-contract.md` § 7.2
 * R9) — rather than in either composable. Two fields rather than one because the treatments
 * genuinely differ, as [CoverVariant.SessionPeekBar] and [CoverVariant.SessionFocus] encode.
 */
data class ActiveSession(
    val session: ReadingSession,
    val book: Book,
    val peekBarCover: CoverUiModel,
    val focusCover: CoverUiModel,
)
