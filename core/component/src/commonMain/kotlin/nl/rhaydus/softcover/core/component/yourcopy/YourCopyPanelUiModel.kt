package nl.rhaydus.softcover.core.component.yourcopy

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** A panel of the reader's private facts about their copy of a book, each row tappable to edit it. */
@Immutable
data class YourCopyPanelUiModel<K>(
    val rows: ImmutableList<YourCopyRowUiModel<K>>,
) {
    companion object : UiModelPreviews<YourCopyPanelUiModel<String>> {
        override val previews: ImmutableMap<String, YourCopyPanelUiModel<String>> = persistentMapOf(
            "Empty" to YourCopyPanelUiModel(
                rows = persistentListOf(
                    YourCopyRowUiModel(
                        key = "acquisition",
                        icon = SoftcoverIcon.ShoppingBag,
                        label = "Where you got it",
                        value = YourCopyValue.Empty(invitation = "Add where you got it"),
                    ),
                ),
            ),
            "Set" to YourCopyPanelUiModel(
                rows = persistentListOf(
                    YourCopyRowUiModel(
                        key = "acquisition",
                        icon = SoftcoverIcon.ShoppingBag,
                        label = "Where you got it",
                        value = YourCopyValue.Set(text = "Bought in a bookshop"),
                    ),
                ),
            ),
            "Priced" to YourCopyPanelUiModel(
                rows = persistentListOf(
                    YourCopyRowUiModel(
                        key = "acquisition",
                        icon = SoftcoverIcon.ShoppingBag,
                        label = "Where you got it",
                        value = YourCopyValue.Set(text = "Bought in a bookshop"),
                        trailing = "€14.50",
                    ),
                ),
            ),
            "Two rows" to YourCopyPanelUiModel(
                rows = persistentListOf(
                    YourCopyRowUiModel(
                        key = "acquisition",
                        icon = SoftcoverIcon.ShoppingBag,
                        label = "Where you got it",
                        value = YourCopyValue.Set(text = "Bought in a bookshop"),
                    ),
                    YourCopyRowUiModel(
                        key = "format",
                        icon = SoftcoverIcon.LibraryBooks,
                        label = "Format",
                        value = YourCopyValue.Empty(invitation = "Add the format you read"),
                    ),
                ),
            ),
        )
    }
}
