package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.badge.CoverOverlayUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel

/**
 * @property selection `null` is outside selection mode; non-null draws the selection treatment whether or
 * not the card is selected.
 * @property shakeOnError Stays `true` until the collector handles [BookCardEvent.ShakeEnded].
 */
@Immutable
data class BookCardDecorations(
    val progress: BookCardProgress? = null,
    val coverBadge: CoverOverlayUiModel? = null,
    val selection: SelectionMarkUiModel? = null,
    val pressBehaviour: Press = Press.Tap,
    val shakeOnError: Boolean = false,
) {
    /** [None] draws no click target, no hover highlight and no role. */
    enum class Press {
        Tap,
        TapAndLongPress,
        None,
    }
}
