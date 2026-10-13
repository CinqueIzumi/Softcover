package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable

/**
 * @property selection `null` is outside selection mode.
 * @property shakeOnError Stays `true` until the collector handles [BookCardEvent.ShakeEnded].
 */
@Immutable
data class BookCardDecorations(
    val progress: BookCardProgress? = null,
    val selection: BookCardSelection? = null,
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
