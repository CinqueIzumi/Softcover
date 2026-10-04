package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything [ToggleRow] renders: a [title] over an optional [gloss], and a switch showing [checked]. The
 * [title] is the row's accessibility label. A row that must not be used yet is built with `enabled = false`.
 *
 * @property key Identity — [ToggleEvent.Toggled] carries it back.
 * @property variant The row's padding; see [ToggleRowVariant].
 */
@Immutable
data class ToggleRowUiModel<K>(
    val key: K,
    val title: String,
    val gloss: String?,
    val checked: Boolean,
    val variant: ToggleRowVariant = ToggleRowVariant.Plain,
    val enabled: Boolean = true,
) {
    companion object : UiModelPreviews<ToggleRowUiModel<String>> {
        override val previews: ImmutableList<ToggleRowUiModel<String>> = persistentListOf(
            ToggleRowUiModel(
                key = "reading-streak",
                title = "Reading streak",
                gloss = "Count the days you read in a row, shown on your profile.",
                checked = true,
                variant = ToggleRowVariant.Inset,
            ),
            ToggleRowUiModel(
                key = "show-titles",
                title = "Show titles & authors",
                gloss = null,
                checked = false,
            ),
            ToggleRowUiModel(
                key = "dynamic-colour",
                title = "Dynamic colour",
                gloss = "Take the whole scheme from your wallpaper instead of a spine colour.",
                checked = false,
                enabled = false,
            ),
        )
    }
}
