package nl.rhaydus.softcover.core.component.selection

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [SelectionMark] renders: whether the mark reads as selected, and what it sits over —
 * see [SelectionMarkBackdrop].
 *
 * @property selected Selected shows a filled circle with a check mark; unselected shows an empty
 * circle with no icon.
 * @property backdrop What the unselected ring's fill reads against — see [SelectionMarkBackdrop].
 */
@Immutable
data class SelectionMarkUiModel(
    val selected: Boolean,
    val backdrop: SelectionMarkBackdrop,
) {
    companion object : UiModelPreviews<SelectionMarkUiModel> {
        /** Per R5: both anatomy branches — selected and unselected — for every backdrop. */
        override val previews: ImmutableList<SelectionMarkUiModel> = persistentListOf(
            SelectionMarkUiModel(
                selected = true,
                backdrop = SelectionMarkBackdrop.Image,
            ),
            SelectionMarkUiModel(
                selected = false,
                backdrop = SelectionMarkBackdrop.Image,
            ),
            SelectionMarkUiModel(
                selected = true,
                backdrop = SelectionMarkBackdrop.Surface,
            ),
            SelectionMarkUiModel(
                selected = false,
                backdrop = SelectionMarkBackdrop.Surface,
            ),
        )
    }
}
