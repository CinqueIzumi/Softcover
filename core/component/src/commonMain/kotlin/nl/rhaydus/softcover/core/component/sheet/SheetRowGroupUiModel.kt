package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * A row selection runs the sheet's animated dismiss after [SheetRowEvent.Selected], so the host's
 * handler must not close the sheet itself. When [dismissesSheet] is false the group leaves the
 * sheet open and the host closes it through [SheetSession.requestClose], for a host that
 * must apply the selection before the hide starts.
 */
@Immutable
data class SheetRowGroupUiModel<K>(
    val rows: ImmutableList<SheetRowUiModel<K>>,
    val dismissesSheet: Boolean = true,
) {
    companion object : UiModelPreviews<SheetRowGroupUiModel<String>> {
        override val previews: ImmutableMap<String, SheetRowGroupUiModel<String>> = persistentMapOf(
            "Single choice" to SheetRowGroupUiModel(
                rows = persistentListOf(
                    SheetRowUiModel(
                        key = "title",
                        label = "Title",
                        selected = false,
                    ),
                    SheetRowUiModel(
                        key = "author",
                        label = "Author",
                        selected = true,
                    ),
                    SheetRowUiModel(
                        key = "date-added",
                        label = "Date added",
                        selected = false,
                    ),
                ),
            ),
        )
    }
}
