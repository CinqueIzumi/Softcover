package nl.rhaydus.softcover.feature.widget.placed

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/** Signals that a widget instance was added or removed, which no widget store observes. */
internal class WidgetInstanceChanges {
    private val signal = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val changes: Flow<Unit> = signal

    fun notifyChanged() {
        signal.tryEmit(Unit)
    }
}
