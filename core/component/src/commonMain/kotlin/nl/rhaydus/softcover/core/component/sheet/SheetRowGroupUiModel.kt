package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * [dismissesSheetOnSelect] makes the group run the sheet's animated dismiss after emitting
 * [SheetRowEvent.Selected], so the host's handler must not close the sheet itself.
 */
@Immutable
data class SheetRowGroupUiModel<K>(
    val rows: ImmutableList<SheetRowUiModel<K>>,
    val dismissesSheetOnSelect: Boolean = false,
) {
    companion object : UiModelPreviews<SheetRowGroupUiModel<String>> {
        override val previews: ImmutableList<SheetRowGroupUiModel<String>> = persistentListOf(
            SheetRowGroupUiModel(rows = SheetRowUiModel.previews),
        )
    }
}
