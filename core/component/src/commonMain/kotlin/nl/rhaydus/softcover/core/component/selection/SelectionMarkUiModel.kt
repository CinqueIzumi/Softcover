package nl.rhaydus.softcover.core.component.selection

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [SelectionMark] renders: whether the mark reads as selected, and which surface's size
 * and icon size apply — see [SelectionMarkVariant].
 *
 * @property selected Selected shows a filled circle with a check mark; unselected shows an empty
 * circle with no icon.
 * @property variant Which surface's size/icon size apply — see [SelectionMarkVariant].
 */
@Immutable
data class SelectionMarkUiModel(
    val selected: Boolean,
    val variant: SelectionMarkVariant,
) {
    companion object : UiModelPreviews<SelectionMarkUiModel> {
        /** Per R5: both anatomy branches — selected and unselected — for every variant. */
        override val previews: ImmutableList<SelectionMarkUiModel> = persistentListOf(
            SelectionMarkUiModel(
                selected = true,
                variant = SelectionMarkVariant.OnCover,
            ),
            SelectionMarkUiModel(
                selected = false,
                variant = SelectionMarkVariant.OnCover,
            ),
            SelectionMarkUiModel(
                selected = true,
                variant = SelectionMarkVariant.RowLeading,
            ),
            SelectionMarkUiModel(
                selected = false,
                variant = SelectionMarkVariant.RowLeading,
            ),
            SelectionMarkUiModel(
                selected = true,
                variant = SelectionMarkVariant.EditionCard,
            ),
            SelectionMarkUiModel(
                selected = false,
                variant = SelectionMarkVariant.EditionCard,
            ),
        )
    }
}
