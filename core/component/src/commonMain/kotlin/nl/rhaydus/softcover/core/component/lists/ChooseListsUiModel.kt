package nl.rhaydus.softcover.core.component.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews

/**
 * Everything `ChooseListsBottomSheet` renders: who is being shelved, and the reader's custom lists
 * with each one's membership relative to that selection.
 *
 * Which lists to offer is the host's decision, not the mapping's — [rows] is exactly what the sheet
 * shows, in the order it shows them.
 */
@Immutable
data class ChooseListsUiModel(
    val variant: ChooseListsVariant,
    val rows: ImmutableList<ChooseListsRowUiModel>,
) {
    companion object : UiModelPreviews<ChooseListsUiModel> {
        /**
         * Per R5, one fixture per branch that changes the anatomy: the single-book header (one
         * upright jacket, "tap to file it here" copy) against the bulk header (a rotated stack, and
         * captions that count the selection), plus the empty case, which replaces the whole list
         * with the "No custom lists yet" state.
         */
        override val previews: ImmutableList<ChooseListsUiModel> = persistentListOf(
            ChooseListsUiModel(
                variant = ChooseListsVariant.SingleBook(name = "Piranesi"),
                rows = persistentListOf(
                    ChooseListsRowUiModel(
                        listId = 1,
                        name = "Winter reading",
                        caption = "12 BOOKS",
                        actionLabel = "On the list",
                        membership = ListMembership.ALL,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 2,
                        name = "Second helpings",
                        caption = "4 BOOKS",
                        actionLabel = "Add",
                        membership = ListMembership.NONE,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 3,
                        name = "Bought in a hurry",
                        caption = "31 BOOKS",
                        actionLabel = "Add",
                        membership = ListMembership.NONE,
                        isPending = true,
                    ),
                ),
            ),
            ChooseListsUiModel(
                variant = ChooseListsVariant.ManyBooks(
                    bookCount = 5,
                    coverCount = 3,
                ),
                rows = persistentListOf(
                    ChooseListsRowUiModel(
                        listId = 1,
                        name = "Winter reading",
                        caption = "12 BOOKS · ALL 5 HERE",
                        actionLabel = "On all 5",
                        membership = ListMembership.ALL,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 2,
                        name = "Second helpings",
                        caption = "4 BOOKS · 3 OF 5 HERE",
                        actionLabel = "Add the other 2",
                        membership = ListMembership.PARTIAL,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 3,
                        name = "Bought in a hurry",
                        caption = "31 BOOKS · NONE YET",
                        actionLabel = "Add all 5",
                        membership = ListMembership.NONE,
                        isPending = false,
                    ),
                ),
            ),
            ChooseListsUiModel(
                variant = ChooseListsVariant.SingleBook(name = "The Name of the Wind"),
                rows = persistentListOf(),
            ),
        )
    }
}
