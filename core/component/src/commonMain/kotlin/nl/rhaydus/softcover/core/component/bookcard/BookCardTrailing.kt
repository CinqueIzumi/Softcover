package nl.rhaydus.softcover.core.component.bookcard

import androidx.compose.runtime.Immutable
import nl.rhaydus.softcover.core.component.badge.BadgeUiModel
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.component.control.ToggleUiModel
import nl.rhaydus.softcover.core.component.control.ToggleVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * [Badge] and [Restore] are read-only — a hidden row restores through the card's own tap. [Bookmark] and
 * [SetProgress] are their own tap targets and emit [BookCardEvent.TrailingClicked].
 */
@Immutable
sealed interface BookCardTrailing {
    data class Badge(val model: BadgeUiModel) : BookCardTrailing

    data class Restore(val label: String) : BookCardTrailing

    data class Bookmark(val toggle: ToggleUiModel) : BookCardTrailing {
        companion object {
            fun labelled(
                saved: Boolean,
                contentDescription: String,
                enabled: Boolean = true,
            ): Bookmark = Bookmark(
                toggle = ToggleUiModel(
                    key = "bookmark",
                    checked = saved,
                    description = contentDescription,
                    variant = ToggleVariant.Filled,
                    on = SoftcoverIcon.BookmarkAdded,
                    off = SoftcoverIcon.BookmarkAdd,
                    enabled = enabled,
                ),
            )
        }
    }

    data class SetProgress(val pill: PillButtonUiModel) : BookCardTrailing {
        companion object {
            fun labelled(enabled: Boolean = true): SetProgress = SetProgress(
                pill = PillButtonUiModel(
                    key = "set-progress",
                    content = PillButtonContent.IconOnly(
                        icon = SoftcoverIcon.Edit,
                        description = "Set progress",
                    ),
                    tone = PillButtonTone.Accent,
                    enabled = enabled,
                ),
            )
        }
    }
}
