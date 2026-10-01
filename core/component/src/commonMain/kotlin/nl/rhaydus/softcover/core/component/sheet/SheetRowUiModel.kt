package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
data class SheetRowUiModel<K>(
    val key: K,
    val label: String,
    val selected: Boolean,
    val leadingIcon: SoftcoverIcon? = null,
    val count: String? = null,
    val countDescription: String? = null,
) {
    companion object : UiModelPreviews<SheetRowUiModel<String>> {
        override val previews: ImmutableList<SheetRowUiModel<String>> = persistentListOf(
            SheetRowUiModel(
                key = "unselected",
                label = "Fantasy",
                selected = false,
            ),
            SheetRowUiModel(
                key = "selected",
                label = "Science fiction",
                selected = true,
            ),
            SheetRowUiModel(
                key = "icon-and-count",
                label = "Currently reading",
                selected = false,
                leadingIcon = SoftcoverIcon.Bookmark,
                count = "12",
                countDescription = "12 books",
            ),
            SheetRowUiModel(
                key = "selected-icon-and-count",
                label = "Want to read",
                selected = true,
                leadingIcon = SoftcoverIcon.BookmarkCheck,
                count = "48",
                countDescription = "48 books",
            ),
        )
    }
}
