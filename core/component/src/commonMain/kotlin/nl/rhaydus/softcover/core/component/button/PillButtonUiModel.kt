package nl.rhaydus.softcover.core.component.button

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [PillButton] renders: a fixed-height pill coloured by [tone], dimmed and inert when not [enabled].
 *
 * @property key Identity — [PillButtonEvent.Clicked] carries it back so one handler can serve several pills.
 */
@Immutable
data class PillButtonUiModel(
    val key: String,
    val content: PillButtonContent,
    val tone: PillButtonTone = PillButtonTone.Tonal,
    val size: PillButtonSize = PillButtonSize.Regular,
    val enabled: Boolean = true,
) {
    companion object : UiModelPreviews<PillButtonUiModel> {
        override val previews: ImmutableMap<String, PillButtonUiModel> = persistentMapOf(
            "Large Tonal" to PillButtonUiModel(
                key = "tonal-label",
                content = PillButtonContent.Label(
                    text = "Add to list",
                    leadingIcon = SoftcoverIcon.BookmarkAdd,
                ),
                size = PillButtonSize.Large,
            ),
            "Icon only, Tonal" to PillButtonUiModel(
                key = "icon-only",
                content = PillButtonContent.IconOnly(
                    icon = SoftcoverIcon.CheckCircle,
                    description = "Select books",
                ),
            ),
            "Regular Accent" to PillButtonUiModel(
                key = "accent",
                content = PillButtonContent.Label(
                    text = "Update",
                    leadingIcon = SoftcoverIcon.Edit,
                ),
                tone = PillButtonTone.Accent,
            ),
            "Regular Filled" to PillButtonUiModel(
                key = "filled",
                content = PillButtonContent.Label(text = "Add"),
                tone = PillButtonTone.Filled,
            ),
            "Regular Outlined" to PillButtonUiModel(
                key = "outlined",
                content = PillButtonContent.Label(
                    text = "Bookshop",
                    leadingIcon = SoftcoverIcon.Storefront,
                ),
                tone = PillButtonTone.Outlined,
            ),
            "Quiet, with indicator" to PillButtonUiModel(
                key = "quiet-indicator",
                content = PillButtonContent.Label(
                    text = "Filter",
                    leadingIcon = SoftcoverIcon.FilterList,
                    indicator = true,
                ),
                tone = PillButtonTone.Quiet,
            ),
            "Regular Tonal" to PillButtonUiModel(
                key = "regular-tonal",
                content = PillButtonContent.Label(
                    text = "Sort",
                    leadingIcon = SoftcoverIcon.Sort,
                ),
            ),
            "Disabled" to PillButtonUiModel(
                key = "disabled",
                content = PillButtonContent.Label(
                    text = "Move",
                    leadingIcon = SoftcoverIcon.Bookmark,
                ),
                enabled = false,
            ),
        )
    }
}
