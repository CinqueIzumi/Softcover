package nl.rhaydus.softcover.core.component.control

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [Toggle] renders: a two-state glyph control exposed to accessibility as a switch.
 *
 * @property on The glyph shown while [checked]; [off] the glyph otherwise.
 * @property description The accessibility label: the action a tap performs now, which changes with
 * [checked] and doubles as the tooltip in both variants.
 * There is no loading state: a toggle that must not be used yet is built with `enabled = false`.
 */
@Immutable
data class ToggleUiModel(
    val checked: Boolean,
    val description: String,
    val variant: ToggleVariant,
    val on: SoftcoverIcon,
    val off: SoftcoverIcon,
    val enabled: Boolean = true,
) {
    companion object : UiModelPreviews<ToggleUiModel> {
        override val previews: ImmutableMap<String, ToggleUiModel> = persistentMapOf(
            "Filled icon, on" to ToggleUiModel(
                checked = true,
                description = "Saved to your library",
                variant = ToggleVariant.Filled,
                on = SoftcoverIcon.BookmarkAdded,
                off = SoftcoverIcon.BookmarkAdd,
            ),
            "Filled icon, off" to ToggleUiModel(
                checked = false,
                description = "Save to your library",
                variant = ToggleVariant.Filled,
                on = SoftcoverIcon.BookmarkAdded,
                off = SoftcoverIcon.BookmarkAdd,
            ),
            "Filled icon, disabled" to ToggleUiModel(
                checked = false,
                description = "Save to your library",
                variant = ToggleVariant.Filled,
                on = SoftcoverIcon.BookmarkAdded,
                off = SoftcoverIcon.BookmarkAdd,
                enabled = false,
            ),
            "Bare icon, on" to ToggleUiModel(
                checked = true,
                description = "Hide Reading from the library tabs",
                variant = ToggleVariant.Bare,
                on = SoftcoverIcon.Visibility,
                off = SoftcoverIcon.VisibilityOff,
            ),
            "Bare icon, off" to ToggleUiModel(
                checked = false,
                description = "Show Reading on the library tabs",
                variant = ToggleVariant.Bare,
                on = SoftcoverIcon.Visibility,
                off = SoftcoverIcon.VisibilityOff,
            ),
        )
    }
}
