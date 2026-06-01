package nl.rhaydus.softcover.core.presentation.util

import androidx.compose.runtime.IntState
import androidx.compose.runtime.mutableIntStateOf

/**
 * Process-wide signal for a one-shot pulse on a bottom-bar tab icon.
 *
 * Surfaces that complete a cross-tab transition (e.g. marking a book as read on the
 * Reading tab, which lands the book on the Library shelf) call [pulseLibrary] to nudge
 * the destination tab icon. The bottom-navigation composables observe [libraryPulseKey];
 * every increment fires a single scale pulse.
 *
 * The signal is intentionally non-persistent — it is a UI event, not state. Each value
 * change is an edge that downstream consumers react to once.
 */
object BottomBarPulseManager {
    private val _libraryPulseKey = mutableIntStateOf(value = 0)

    val libraryPulseKey: IntState = _libraryPulseKey

    fun pulseLibrary() {
        _libraryPulseKey.intValue += 1
    }
}
