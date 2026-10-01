package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Latches once the sheet starts its animated hide, so a second tap during the hide cannot emit a second
 * event.
 */
@Stable
internal class SheetDismissal(private val hide: () -> Unit) {
    var started: Boolean by mutableStateOf(false)
        private set

    fun start() {
        if (started) return

        started = true
        hide()
    }
}
