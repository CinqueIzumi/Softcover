package nl.rhaydus.softcover.core.component.sheet

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

@Immutable
data class SheetActionRowUiModel<K>(
    val key: K,
    val label: String,
    val description: String,
    val icon: SoftcoverIcon,
) {
    companion object : UiModelPreviews<SheetActionRowUiModel<String>> {
        override val previews: ImmutableMap<String, SheetActionRowUiModel<String>> = persistentMapOf(
            "Action" to SheetActionRowUiModel(
                key = "hide-book",
                label = "Hide this book",
                description = "Just this title stops showing up here",
                icon = SoftcoverIcon.VisibilityOff,
            ),
            "Long label" to SheetActionRowUiModel(
                key = "hide-series",
                label = "Hide everything from The Wheel of Time",
                description = "Hide every book in this series from Up next",
                icon = SoftcoverIcon.LibraryBooks,
            ),
        )
    }
}
