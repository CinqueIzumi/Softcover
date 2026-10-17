package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable

@Immutable
sealed interface BookCardProgress {
    /** Keeps the slot's height so a grid mixing in-progress and untouched books never jumps a row. */
    data object Reserved : BookCardProgress

    /**
     * @property label `null` draws the bar alone.
     */
    data class Value(
        val fraction: Float,
        val label: String?,
    ) : BookCardProgress
}
