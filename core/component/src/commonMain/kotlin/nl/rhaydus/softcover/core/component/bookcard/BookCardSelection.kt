package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Immutable
sealed interface BookCardSelection {
    val selected: Boolean

    /** Every card draws the mark and an unselected card is dimmed; the card announces a checkbox. */
    data class Multiple(override val selected: Boolean) : BookCardSelection

    /**
     * Only the selected card draws the ring and the mark; the others draw nothing and are not dimmed. The
     * card announces a radio button.
     */
    data class Single(override val selected: Boolean) : BookCardSelection
}

/** `null` when the card draws no mark. */
internal val BookCardSelection.markSelected: Boolean?
    get() = when (this) {
        is BookCardSelection.Multiple -> selected
        is BookCardSelection.Single -> true.takeIf { selected }
    }

internal val BookCardSelection.dimsUnselected: Boolean
    get() = this is BookCardSelection.Multiple && selected.not()

@Composable
internal fun rememberHeldMark(markSelected: Boolean?): Boolean {
    var held by remember { mutableStateOf(markSelected == true) }

    if (markSelected != null) {
        held = markSelected
    }

    return held
}
