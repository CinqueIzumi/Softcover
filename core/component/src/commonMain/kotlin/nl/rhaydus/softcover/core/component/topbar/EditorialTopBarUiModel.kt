package nl.rhaydus.softcover.core.component.topbar

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [EditorialTopBar] renders.
 *
 * @property title The page's name in the editorial headline; `null` leaves the bar to its back
 * button and actions, for a page whose content already names itself.
 */
@Immutable
data class EditorialTopBarUiModel(
    val title: String? = null,
) {
    companion object : UiModelPreviews<EditorialTopBarUiModel> {
        override val previews: ImmutableList<EditorialTopBarUiModel> = persistentListOf(
            EditorialTopBarUiModel(title = "Appearance"),
            EditorialTopBarUiModel(),
        )
    }
}
