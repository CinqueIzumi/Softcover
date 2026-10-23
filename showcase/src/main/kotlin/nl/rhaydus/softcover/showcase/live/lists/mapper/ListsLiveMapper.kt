package nl.rhaydus.softcover.showcase.live.lists.mapper

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.lists.ChooseListsUiModel
import nl.rhaydus.softcover.core.component.lists.ListMembership
import nl.rhaydus.softcover.core.component.sheet.SheetActionRowUiModel
import nl.rhaydus.softcover.core.component.topbar.TopBarUiModel
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon
import nl.rhaydus.softcover.showcase.live.lists.model.ListsDemo

internal fun listsLiveTopBar(): TopBarUiModel = TopBarUiModel(title = "Lists")

internal fun listsDemos(): ImmutableList<SheetActionRowUiModel<ListsDemo>> = persistentListOf(
    SheetActionRowUiModel(
        key = ListsDemo.ONE_BOOK,
        label = "Shelve one book",
        description = "Tap a list to put the book on it or take it off.",
        icon = SoftcoverIcon.BookmarkAdd,
    ),
    SheetActionRowUiModel(
        key = ListsDemo.SEVERAL_BOOKS,
        label = "Shelve several books",
        description = "A list that already holds some of them offers to add the rest.",
        icon = SoftcoverIcon.LibraryBooks,
    ),
    SheetActionRowUiModel(
        key = ListsDemo.NO_LISTS,
        label = "No lists yet",
        description = "The empty state, with the way to start a list.",
        icon = SoftcoverIcon.Add,
    ),
)

internal fun listsDemoModel(demo: ListsDemo): ChooseListsUiModel = ChooseListsUiModel.previews.getValue(
    when (demo) {
        ListsDemo.ONE_BOOK -> "Lists"
        ListsDemo.SEVERAL_BOOKS -> "Part-filled list"
        ListsDemo.NO_LISTS -> "No lists yet"
    },
)

internal fun ChooseListsUiModel.togglingMembership(listId: Int): ChooseListsUiModel = copy(
    rows = rows
        .map { row ->
            if (row.listId == listId) {
                val membership = when (row.membership) {
                    ListMembership.ALL -> ListMembership.NONE
                    ListMembership.NONE,
                    ListMembership.PARTIAL -> ListMembership.ALL
                }

                row.copy(
                    membership = membership,
                    chip = chipFor(membership = membership),
                )
            } else {
                row
            }
        }
        .toImmutableList(),
)

private fun chipFor(membership: ListMembership): ChipUiModel = when (membership) {
    ListMembership.ALL -> ChipUiModel(
        key = "on-the-list",
        label = "On the list",
        tone = ChipTone.Container,
        interaction = ChipInteraction.Inert,
        trailing = ChipTrailing.Icon(
            icon = SoftcoverIcon.Close,
            description = "Remove from list",
        ),
    )

    ListMembership.NONE,
    ListMembership.PARTIAL -> ChipUiModel(
        key = "add",
        label = "Add",
        tone = ChipTone.Outlined,
        interaction = ChipInteraction.Inert,
        leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
    )
}
