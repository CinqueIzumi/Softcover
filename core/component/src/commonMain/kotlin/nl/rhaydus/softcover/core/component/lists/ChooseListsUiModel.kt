package nl.rhaydus.softcover.core.component.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetFooterButton
import nl.rhaydus.softcover.core.component.sheet.SheetFooterUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
import nl.rhaydus.softcover.core.component.sheet.SheetModel
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.component.state.EmptyStateVariant
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything `ChooseListsBottomSheet` renders: the [scaffold] chrome naming who is being shelved, and
 * the reader's custom lists with each one's membership relative to that selection.
 *
 * Which lists to offer is the host's decision, not the mapping's — [rows] is exactly what the sheet
 * shows, in the order it shows them.
 */
@Immutable
data class ChooseListsUiModel(
    val rows: ImmutableList<ChooseListsRowUiModel>,
    val scaffold: SheetScaffoldUiModel,
    val emptyState: EmptyStateUiModel = EmptyStateUiModel(
        title = "No custom lists yet",
        body = "Create your first list below.",
        variant = EmptyStateVariant.Inline,
    ),
) : SheetModel<ChooseListsUiModel> {
    override val closeRequested: Boolean get() = scaffold.closeRequested

    override fun requestingClose(): ChooseListsUiModel = copy(scaffold = scaffold.requestingClose())

    companion object : UiModelPreviews<ChooseListsUiModel> {
        private val scaffold = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = "Lists",
                    headline = "Shelve Piranesi",
                ),
            ),
            dismissLabel = "Done",
            footer = SheetFooterUiModel.Stacked(
                primary = null,
                secondary = SheetFooterButton(label = "New list"),
            ),
            layout = SheetLayout.Pinned,
        )

        private val onTheList = ChipUiModel(
            key = "on-the-list",
            label = "On the list",
            tone = ChipTone.Container,
            interaction = ChipInteraction.Inert,
            trailing = ChipTrailing.Icon(
                icon = SoftcoverIcon.Close,
                description = "Remove from list",
            ),
        )

        private val add = ChipUiModel(
            key = "add",
            label = "Add",
            tone = ChipTone.Outlined,
            interaction = ChipInteraction.Inert,
            leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
        )

        override val previews: ImmutableMap<String, ChooseListsUiModel> = persistentMapOf(
            "Lists" to ChooseListsUiModel(
                rows = persistentListOf(
                    ChooseListsRowUiModel(
                        listId = 1,
                        name = "Winter reading",
                        caption = "12 BOOKS",
                        chip = onTheList,
                        membership = ListMembership.ALL,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 2,
                        name = "Second helpings",
                        caption = "5 BOOKS",
                        chip = add,
                        membership = ListMembership.NONE,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 3,
                        name = "Borrowed from Mara",
                        caption = "3 BOOKS",
                        chip = add,
                        membership = ListMembership.NONE,
                        isPending = true,
                    ),
                ),
                scaffold = scaffold,
            ),
            "Part-filled list" to ChooseListsUiModel(
                rows = persistentListOf(
                    ChooseListsRowUiModel(
                        listId = 1,
                        name = "Winter reading",
                        caption = "12 BOOKS · 2 OF 3 HERE",
                        chip = add.copy(
                            tone = ChipTone.Filled,
                            label = "Add the other 1",
                        ),
                        membership = ListMembership.PARTIAL,
                        isPending = false,
                    ),
                ),
                scaffold = scaffold.copy(
                    header = SheetHeaderUiModel(
                        section = SectionHeaderUiModel.Section(
                            eyebrow = "Lists",
                            headline = "Shelve 3 books",
                        ),
                    ),
                ),
            ),
            "No lists yet" to ChooseListsUiModel(
                rows = persistentListOf(),
                scaffold = scaffold,
            ),
        )
    }
}
