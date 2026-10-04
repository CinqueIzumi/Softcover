package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [Toggle] renders: a two-state control exposed to accessibility as a switch.
 *
 * @property key Identity — [ToggleEvent.Toggled] carries it back.
 * @property description The accessibility label. A [ToggleVariant.Switch] takes a fixed name for what it
 * controls, because a screen reader already announces the on/off state; a [ToggleVariant.Icon] takes the
 * action a tap performs now, which changes with [checked] and doubles as the tooltip.
 * There is no loading state: a toggle that must not be used yet is built with `enabled = false`.
 */
@Immutable
data class ToggleUiModel(
    val key: String,
    val checked: Boolean,
    val description: String,
    val variant: ToggleVariant,
    val enabled: Boolean = true,
) {
    companion object : UiModelPreviews<ToggleUiModel> {
        override val previews: ImmutableList<ToggleUiModel> = persistentListOf(
            ToggleUiModel(
                key = "bookmark-saved",
                checked = true,
                description = "Saved to your library",
                variant = ToggleVariant.Icon(
                    on = SoftcoverIcon.BookmarkAdded,
                    off = SoftcoverIcon.BookmarkAdd,
                    style = ToggleIconStyle.Filled,
                ),
            ),
            ToggleUiModel(
                key = "bookmark-unsaved",
                checked = false,
                description = "Save to your library",
                variant = ToggleVariant.Icon(
                    on = SoftcoverIcon.BookmarkAdded,
                    off = SoftcoverIcon.BookmarkAdd,
                    style = ToggleIconStyle.Filled,
                ),
            ),
            ToggleUiModel(
                key = "bookmark-loading",
                checked = false,
                description = "Save to your library",
                variant = ToggleVariant.Icon(
                    on = SoftcoverIcon.BookmarkAdded,
                    off = SoftcoverIcon.BookmarkAdd,
                    style = ToggleIconStyle.Filled,
                ),
                enabled = false,
            ),
            ToggleUiModel(
                key = "eye-visible",
                checked = true,
                description = "Hide Reading from the library tabs",
                variant = ToggleVariant.Icon(
                    on = SoftcoverIcon.Visibility,
                    off = SoftcoverIcon.VisibilityOff,
                    style = ToggleIconStyle.Bare,
                ),
            ),
            ToggleUiModel(
                key = "eye-hidden",
                checked = false,
                description = "Show Reading on the library tabs",
                variant = ToggleVariant.Icon(
                    on = SoftcoverIcon.Visibility,
                    off = SoftcoverIcon.VisibilityOff,
                    style = ToggleIconStyle.Bare,
                ),
            ),
            ToggleUiModel(
                key = "switch-on",
                checked = true,
                description = "Hide untagged authors",
                variant = ToggleVariant.Switch,
            ),
            ToggleUiModel(
                key = "switch-disabled",
                checked = false,
                description = "Hide untagged authors",
                variant = ToggleVariant.Switch,
                enabled = false,
            ),
        )
    }
}
