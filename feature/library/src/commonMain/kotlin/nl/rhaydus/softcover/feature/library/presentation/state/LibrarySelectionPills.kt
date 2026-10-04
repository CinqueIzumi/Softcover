package nl.rhaydus.softcover.feature.library.presentation.state

import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonSize
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/** The pills in the selection-mode header, all disabled together while a bulk action is in flight. */
internal data class LibrarySelectionPills(
    val exit: PillButtonUiModel,
    val move: PillButtonUiModel,
    val addToList: PillButtonUiModel,
    val remove: PillButtonUiModel,
) {
    companion object {
        fun of(bulkActionInProgress: Boolean): LibrarySelectionPills {
            return LibrarySelectionPills(
                exit = PillButtonUiModel(
                    key = "exit",
                    content = PillButtonContent.IconOnly(
                        icon = SoftcoverIcon.Close,
                        description = "Exit selection mode",
                    ),
                    enabled = bulkActionInProgress.not(),
                ),
                move = PillButtonUiModel(
                    key = "move",
                    content = PillButtonContent.Label(
                        text = "Move",
                        leadingIcon = SoftcoverIcon.Bookmark,
                    ),
                    size = PillButtonSize.Large,
                    enabled = bulkActionInProgress.not(),
                ),
                addToList = PillButtonUiModel(
                    key = "add-to-list",
                    content = PillButtonContent.Label(
                        text = "Add to list",
                        leadingIcon = SoftcoverIcon.BookmarkAdd,
                    ),
                    size = PillButtonSize.Large,
                    enabled = bulkActionInProgress.not(),
                ),
                remove = PillButtonUiModel(
                    key = "remove",
                    content = PillButtonContent.IconOnly(
                        icon = SoftcoverIcon.Delete,
                        description = "Remove selected books from library",
                    ),
                    tone = PillButtonTone.Accent,
                    size = PillButtonSize.Large,
                    enabled = bulkActionInProgress.not(),
                ),
            )
        }
    }
}
