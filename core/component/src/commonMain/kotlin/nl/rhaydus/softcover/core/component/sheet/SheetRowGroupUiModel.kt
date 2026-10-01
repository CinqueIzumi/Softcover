package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

@Immutable
data class SheetRowGroupUiModel<K>(
    val rows: ImmutableList<SheetRowUiModel<K>>,
) {
    companion object : UiModelPreviews<SheetRowGroupUiModel<String>> {
        override val previews: ImmutableList<SheetRowGroupUiModel<String>> = persistentListOf(
            SheetRowGroupUiModel(rows = SheetRowUiModel.previews),
        )
    }
}
