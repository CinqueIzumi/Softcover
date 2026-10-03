package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.selection.SelectionMarkBackdrop
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel

@Immutable
sealed interface BookCardSelection {
    val selected: Boolean

    /** Every card draws the mark and an unselected card is dimmed; the card announces a checkbox. */
    data class Multiple(val mark: SelectionMarkUiModel) : BookCardSelection {
        override val selected: Boolean get() = mark.selected
    }

    /**
     * Only the selected card draws the ring and the mark; the others draw nothing and are not dimmed. The
     * card announces a radio button.
     */
    data class Single(override val selected: Boolean) : BookCardSelection
}

internal val BookCardSelection.renderedMark: SelectionMarkUiModel?
    get() = when (this) {
        is BookCardSelection.Multiple -> mark
        is BookCardSelection.Single -> SelectionMarkUiModel(
            selected = true,
            backdrop = SelectionMarkBackdrop.Image,
        ).takeIf { selected }
    }

internal val BookCardSelection.dimsUnselected: Boolean
    get() = this is BookCardSelection.Multiple && selected.not()
