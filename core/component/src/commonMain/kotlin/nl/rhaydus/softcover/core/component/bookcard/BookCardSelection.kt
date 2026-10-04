package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable

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
