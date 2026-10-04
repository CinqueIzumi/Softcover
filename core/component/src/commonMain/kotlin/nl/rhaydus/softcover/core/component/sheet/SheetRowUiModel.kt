package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
data class SheetRowUiModel<K>(
    val key: K,
    val label: String,
    val selected: Boolean,
    val leadingIcon: SoftcoverIcon? = null,
    val count: SheetRowCount? = null,
) {
    companion object : UiModelPreviews<SheetRowUiModel<String>> {
        override val previews: ImmutableMap<String, SheetRowUiModel<String>> = persistentMapOf(
            "Unselected" to SheetRowUiModel(
                key = "unselected",
                label = "Fantasy",
                selected = false,
            ),
            "Selected" to SheetRowUiModel(
                key = "selected",
                label = "Science fiction",
                selected = true,
            ),
            "Icon and count" to SheetRowUiModel(
                key = "icon-and-count",
                label = "Currently reading",
                selected = false,
                leadingIcon = SoftcoverIcon.Bookmark,
                count = SheetRowCount(
                    text = "12",
                    description = "12 books",
                ),
            ),
            "Selected, icon and count" to SheetRowUiModel(
                key = "selected-icon-and-count",
                label = "Want to read",
                selected = true,
                leadingIcon = SoftcoverIcon.BookmarkCheck,
                count = SheetRowCount(
                    text = "48",
                    description = "48 books",
                ),
            ),
        )
    }
}
