package nl.rhaydus.softcover.feature.book_detail.presentation.screen.section

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import nl.rhaydus.designsystem.component.DesktopTooltip
import nl.rhaydus.softcover.core.component.text.SoftcoverText
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.core.designsystem.presentation.icon.drawableIconResource
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnClearDeadlineAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnEditionOwnedToggleAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnOpenDeadlinePickerAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnRemoveBookClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShareBookClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowChooseListsSheetAction
import nl.rhaydus.softcover.feature.book_detail.presentation.action.OnShowEditEditionSheetClickAction
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState

@Composable
internal fun BookOverflowMenu(
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    isOnline: Boolean,
    iconColors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
) {
    val book = state.book.takeUnless { state.loadingBookDetails }
    val isKnown = book != null

    var menuOpen by remember(isKnown) { mutableStateOf(false) }
    val dismiss = { menuOpen = false }

    Box {
        DesktopTooltip(text = "More actions") {
            IconButton(
                onClick = { menuOpen = true },
                enabled = isKnown,
                colors = iconColors,
            ) {
                val moreActionsIcon = drawableIconResource(
                    icon = SoftcoverIcon.Edit,
                    contentDescription = "More actions",
                )

                Icon(
                    painter = moreActionsIcon.getIconPainter(),
                    contentDescription = moreActionsIcon.contentDescription,
                )
            }
        }

        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = dismiss,
        ) {
            book?.let {
                BookOverflowMenuItems(
                    isOnShelf = it.status != BookStatus.None,
                    hasEditions = it.editions.isNotEmpty(),
                    onRemoveClick = {
                        runAction(OnRemoveBookClickAction(book = it))
                    },
                    state = state,
                    runAction = runAction,
                    isOnline = isOnline,
                    dismiss = dismiss,
                )
            }
        }
    }
}

@Composable
private fun BookOverflowMenuItems(
    isOnShelf: Boolean,
    hasEditions: Boolean,
    onRemoveClick: () -> Unit,
    state: BookDetailUiState,
    runAction: (BookDetailAction) -> Unit,
    isOnline: Boolean,
    dismiss: () -> Unit,
) {
    DropdownMenuItem(
        text = { SoftcoverText(text = "Share") },
        leadingIcon = {
            val shareIcon = drawableIconResource(
                icon = SoftcoverIcon.Share,
                contentDescription = "",
            )

            Icon(
                painter = shareIcon.getIconPainter(),
                contentDescription = shareIcon.contentDescription,
            )
        },
        onClick = {
            dismiss()
            runAction(OnShareBookClickAction())
        },
    )

    if (isOnline) {
        DropdownMenuItem(
            text = { SoftcoverText(text = "Choose lists") },
            leadingIcon = {
                val chooseListsIcon = drawableIconResource(
                    icon = SoftcoverIcon.BookmarkAdd,
                    contentDescription = "",
                )

                Icon(
                    painter = chooseListsIcon.getIconPainter(),
                    contentDescription = chooseListsIcon.contentDescription,
                )
            },
            onClick = {
                dismiss()
                runAction(OnShowChooseListsSheetAction())
            },
        )
    }

    // Switching editions is a preview that works without a user book, so it sits above the
    // on-shelf gate. Marking an edition as owned mutates the user's shelf, so it stays below.
    // Hidden when the book has no known editions (e.g. a freshly added-by-ISBN book) — there
    // is nothing to switch between.
    if (isOnline && hasEditions) {
        DropdownMenuItem(
            text = { SoftcoverText(text = "Change edition") },
            leadingIcon = {
                val changeEditionIcon = drawableIconResource(
                    icon = SoftcoverIcon.LibraryBooks,
                    contentDescription = "",
                )

                Icon(
                    painter = changeEditionIcon.getIconPainter(),
                    contentDescription = changeEditionIcon.contentDescription,
                )
            },
            onClick = {
                dismiss()
                runAction(OnShowEditEditionSheetClickAction())
            },
        )
    }

    // Owning an edition is a list operation, independent of the reading shelf, so it stays
    // available off-shelf (e.g. for a scanned edition) and sits above the on-shelf gate.
    // The displayed edition — the scanned one when arriving from a scan — is what it acts on.
    if (isOnline) {
        state.displayedEdition?.let { ownedEdition ->
            val isOwned = state.isEditionOwned(edition = ownedEdition)

            val ownedLabel = if (isOwned) "Unmark as owned" else "Mark as owned"
            val ownedToggleIcon = drawableIconResource(
                icon = if (isOwned) SoftcoverIcon.Close else SoftcoverIcon.Check,
                contentDescription = "",
            )

            DropdownMenuItem(
                text = { SoftcoverText(text = ownedLabel) },
                leadingIcon = {
                    Icon(
                        painter = ownedToggleIcon.getIconPainter(),
                        contentDescription = ownedToggleIcon.contentDescription,
                    )
                },
                onClick = {
                    dismiss()
                    runAction(
                        OnEditionOwnedToggleAction(
                            edition = ownedEdition,
                            owned = isOwned.not(),
                        ),
                    )
                },
            )
        }
    }

    if (isOnShelf.not()) return

    if (state.deadline == null) {
        DropdownMenuItem(
            text = { SoftcoverText(text = "Set deadline") },
            leadingIcon = {
                val setDeadlineIcon = drawableIconResource(
                    icon = SoftcoverIcon.DateRange,
                    contentDescription = "",
                )

                Icon(
                    painter = setDeadlineIcon.getIconPainter(),
                    contentDescription = setDeadlineIcon.contentDescription,
                )
            },
            onClick = {
                dismiss()
                runAction(OnOpenDeadlinePickerAction())
            },
        )
    } else {
        DropdownMenuItem(
            text = { SoftcoverText(text = "Edit deadline") },
            leadingIcon = {
                val editDeadlineIcon = drawableIconResource(
                    icon = SoftcoverIcon.DateRange,
                    contentDescription = "",
                )

                Icon(
                    painter = editDeadlineIcon.getIconPainter(),
                    contentDescription = editDeadlineIcon.contentDescription,
                )
            },
            onClick = {
                dismiss()
                runAction(OnOpenDeadlinePickerAction())
            },
        )

        DropdownMenuItem(
            text = { SoftcoverText(text = "Clear deadline") },
            leadingIcon = {
                val clearDeadlineIcon = drawableIconResource(
                    icon = SoftcoverIcon.Delete,
                    contentDescription = "",
                )

                Icon(
                    painter = clearDeadlineIcon.getIconPainter(),
                    contentDescription = clearDeadlineIcon.contentDescription,
                )
            },
            onClick = {
                dismiss()
                runAction(OnClearDeadlineAction())
            },
        )
    }

    if (isOnline) {
        DropdownMenuItem(
            text = { SoftcoverText(text = "Remove") },
            leadingIcon = {
                val removeIcon = drawableIconResource(
                    icon = SoftcoverIcon.Delete,
                    contentDescription = "",
                )

                Icon(
                    painter = removeIcon.getIconPainter(),
                    contentDescription = removeIcon.contentDescription,
                )
            },
            onClick = {
                dismiss()
                onRemoveClick()
            },
        )
    }
}
