package nl.rhaydus.softcover.feature.library.presentation.state

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.button.PillButtonContent
import nl.rhaydus.softcover.core.component.button.PillButtonSize
import nl.rhaydus.softcover.core.component.button.PillButtonTone
import nl.rhaydus.softcover.core.component.button.PillButtonUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.domain.model.UserBookStatus

/**
 * The pills in the selection-mode header, all disabled together while a bulk action is in flight,
 * plus the shelves the Move menu offers.
 */
internal data class LibrarySelectionPills(
    val exit: PillButtonUiModel,
    val move: PillButtonUiModel,
    val addToList: PillButtonUiModel,
    val remove: PillButtonUiModel,
    val moveTargets: ImmutableList<MoveTarget>,
) {
    /** One Move menu entry: the shelf [status] it files the selection under and its [label]. */
    data class MoveTarget(
        val status: UserBookStatus,
        val label: String,
    )

    companion object {
        /** [currentShelf] is the status shelf being viewed, left out of the Move menu; `null` on All and list tabs. */
        fun of(
            bulkActionInProgress: Boolean,
            currentShelf: UserBookStatus? = null,
        ): LibrarySelectionPills {
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
                moveTargets = allMoveTargets.filter { it.status != currentShelf }.toImmutableList(),
            )
        }

        private val allMoveTargets = listOf(
            MoveTarget(
                status = UserBookStatus.WANT_TO_READ,
                label = "Move to Want to Read",
            ),
            MoveTarget(
                status = UserBookStatus.CURRENTLY_READING,
                label = "Move to Currently Reading",
            ),
            MoveTarget(
                status = UserBookStatus.PAUSED,
                label = "Move to Paused",
            ),
            MoveTarget(
                status = UserBookStatus.READ,
                label = "Mark as Read",
            ),
        )
    }
}
