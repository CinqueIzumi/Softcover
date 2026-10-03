package nl.rhaydus.softcover.core.component.lists

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.component.chip.ChipInteraction
import nl.rhaydus.softcover.core.component.chip.ChipLeading
import nl.rhaydus.softcover.core.component.chip.ChipSize
import nl.rhaydus.softcover.core.component.chip.ChipTone
import nl.rhaydus.softcover.core.component.chip.ChipTrailing
import nl.rhaydus.softcover.core.component.chip.ChipUiModel
import nl.rhaydus.softcover.core.component.cover.CoverUiModel
import nl.rhaydus.softcover.core.component.cover.CoverVariant
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.component.header.EmphasisTreatment
import nl.rhaydus.softcover.core.component.header.HeaderTextSegment
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderTrailing
import nl.rhaydus.softcover.core.component.sheet.SheetHeaderUiModel
import nl.rhaydus.softcover.core.component.sheet.SheetLayout
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
    val emptyState: EmptyStateUiModel,
) {
    companion object : UiModelPreviews<ChooseListsUiModel> {
        private val fixtureEmptyState = EmptyStateUiModel(
            title = "No custom lists yet",
            body = "Create your first list below.",
            variant = EmptyStateVariant.Inline,
        )

        private fun fixtureScaffold(
            name: String,
            description: String,
            trailing: SheetHeaderTrailing,
        ): SheetScaffoldUiModel = SheetScaffoldUiModel(
            header = SheetHeaderUiModel(
                section = SectionHeaderUiModel.Section(
                    eyebrow = "Lists",
                    headline = persistentListOf(
                        HeaderTextSegment.Plain(text = "Shelve "),
                        HeaderTextSegment.Emphasised(
                            text = name,
                            treatment = EmphasisTreatment.Accent,
                        ),
                    ),
                    description = persistentListOf(HeaderTextSegment.Plain(text = description)),
                ),
                trailing = trailing,
            ),
            layout = SheetLayout.PinnedLazy,
        )

        private fun singleBookScaffold(name: String): SheetScaffoldUiModel = fixtureScaffold(
            name = name,
            description = "Tap a list to file it here — tap a filled one to take it off. Saved as you go.",
            trailing = SheetHeaderTrailing.Jacket(
                cover = CoverUiModel(
                    source = null,
                    coverlessTitle = name,
                    variant = CoverVariant.ChooseListsSingleJacket,
                ),
            ),
        )

        /**
         * One fixture per branch that changes the anatomy: the single-book header (one
         * upright jacket, "tap to file it here" copy) against the bulk header (a rotated stack, and
         * captions that count the selection), plus the empty case, which replaces the whole list
         * with the "No custom lists yet" state.
         */
        override val previews: ImmutableList<ChooseListsUiModel> = persistentListOf(
            ChooseListsUiModel(
                emptyState = fixtureEmptyState,
                scaffold = singleBookScaffold(name = "Piranesi"),
                rows = persistentListOf(
                    ChooseListsRowUiModel(
                        listId = 1,
                        name = "Winter reading",
                        caption = "12 BOOKS",
                        membershipChip = ChipUiModel(
                            key = "1",
                            label = "On the list",
                            tone = ChipTone.Container,
                            trailing = ChipTrailing.Icon(
                                icon = SoftcoverIcon.Close,
                                description = "Remove from list",
                            ),
                            interaction = ChipInteraction.Inert,
                            size = ChipSize.Compact,
                        ),
                        membership = ListMembership.ALL,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 2,
                        name = "Second helpings",
                        caption = "4 BOOKS",
                        membershipChip = ChipUiModel(
                            key = "2",
                            label = "Add",
                            tone = ChipTone.Outlined,
                            leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
                            interaction = ChipInteraction.Inert,
                            size = ChipSize.Compact,
                        ),
                        membership = ListMembership.NONE,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 3,
                        name = "Bought in a hurry",
                        caption = "31 BOOKS",
                        membershipChip = ChipUiModel(
                            key = "3",
                            label = "Add",
                            tone = ChipTone.Outlined,
                            leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
                            interaction = ChipInteraction.Inert,
                            size = ChipSize.Compact,
                        ),
                        membership = ListMembership.NONE,
                        isPending = true,
                    ),
                ),
            ),
            ChooseListsUiModel(
                emptyState = fixtureEmptyState,
                scaffold = fixtureScaffold(
                    name = "5 books",
                    description = "A part-filled list adds the rest first — then a second tap takes all 5 off.",
                    trailing = SheetHeaderTrailing.JacketStack(
                        covers = listOf("Piranesi", "It", "The Name of the Wind").map { title ->
                            CoverUiModel(
                                source = null,
                                coverlessTitle = title,
                                variant = CoverVariant.ChooseListsStackJacket,
                            )
                        }.toImmutableList(),
                    ),
                ),
                rows = persistentListOf(
                    ChooseListsRowUiModel(
                        listId = 1,
                        name = "Winter reading",
                        caption = "12 BOOKS · ALL 5 HERE",
                        membershipChip = ChipUiModel(
                            key = "1",
                            label = "On all 5",
                            tone = ChipTone.Container,
                            trailing = ChipTrailing.Icon(
                                icon = SoftcoverIcon.Close,
                                description = "Remove from list",
                            ),
                            interaction = ChipInteraction.Inert,
                            size = ChipSize.Compact,
                        ),
                        membership = ListMembership.ALL,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 2,
                        name = "Second helpings",
                        caption = "4 BOOKS · 3 OF 5 HERE",
                        membershipChip = ChipUiModel(
                            key = "2",
                            label = "Add the other 2",
                            tone = ChipTone.Filled,
                            leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
                            interaction = ChipInteraction.Inert,
                            size = ChipSize.Compact,
                        ),
                        membership = ListMembership.PARTIAL,
                        isPending = false,
                    ),
                    ChooseListsRowUiModel(
                        listId = 3,
                        name = "Bought in a hurry",
                        caption = "31 BOOKS · NONE YET",
                        membershipChip = ChipUiModel(
                            key = "3",
                            label = "Add all 5",
                            tone = ChipTone.Outlined,
                            leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
                            interaction = ChipInteraction.Inert,
                            size = ChipSize.Compact,
                        ),
                        membership = ListMembership.NONE,
                        isPending = false,
                    ),
                ),
            ),
            ChooseListsUiModel(
                emptyState = fixtureEmptyState,
                scaffold = singleBookScaffold(name = "The Name of the Wind"),
                rows = persistentListOf(),
            ),
        )
    }
}
