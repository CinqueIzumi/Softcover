package nl.rhaydus.softcover.core.component.button

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [PillButton] renders: a fixed-height pill coloured by [tone], gated by [interaction].
 *
 * @property key Identity — [PillButtonEvent.Clicked] carries it back so one handler can serve several pills.
 * @property indicator A trailing dot marking that something behind the pill is active.
 */
@Immutable
data class PillButtonUiModel(
    val key: String,
    val content: PillButtonContent,
    val tone: PillButtonTone = PillButtonTone.Tonal,
    val size: PillButtonSize = PillButtonSize.Regular,
    val interaction: PillButtonInteraction = PillButtonInteraction.Clickable,
    val indicator: Boolean = false,
) {
    companion object : UiModelPreviews<PillButtonUiModel> {
        override val previews: ImmutableList<PillButtonUiModel> = persistentListOf(
            PillButtonUiModel(
                key = "tonal-label",
                content = PillButtonContent.Label(
                    text = "Add to list",
                    leadingIcon = SoftcoverIcon.BookmarkAdd,
                ),
                size = PillButtonSize.Large,
            ),
            PillButtonUiModel(
                key = "icon-only",
                content = PillButtonContent.IconOnly(
                    icon = SoftcoverIcon.CheckCircle,
                    description = "Select books",
                ),
            ),
            PillButtonUiModel(
                key = "accent",
                content = PillButtonContent.Label(
                    text = "Update",
                    leadingIcon = SoftcoverIcon.Edit,
                ),
                tone = PillButtonTone.Accent,
            ),
            PillButtonUiModel(
                key = "filled",
                content = PillButtonContent.Label(text = "Add"),
                tone = PillButtonTone.Filled,
            ),
            PillButtonUiModel(
                key = "outlined",
                content = PillButtonContent.Label(
                    text = "Bookshop",
                    leadingIcon = SoftcoverIcon.Storefront,
                ),
                tone = PillButtonTone.Outlined,
            ),
            PillButtonUiModel(
                key = "quiet-indicator",
                content = PillButtonContent.Label(
                    text = "Filter",
                    leadingIcon = SoftcoverIcon.FilterList,
                ),
                tone = PillButtonTone.Quiet,
                indicator = true,
            ),
            PillButtonUiModel(
                key = "disabled",
                content = PillButtonContent.Label(
                    text = "Move",
                    leadingIcon = SoftcoverIcon.Bookmark,
                ),
                interaction = PillButtonInteraction.Disabled,
            ),
        )
    }
}
