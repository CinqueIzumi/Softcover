package nl.rhaydus.softcover.core.component.chip

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.component.gallery.UiModelPreviews
import nl.rhaydus.softcover.core.designsystem.presentation.icon.SoftcoverIcon

/**
 * Everything [Chip] renders: a label on a fully-rounded surface, coloured by [tone] and [selected],
 * gated by [interaction], with a [leading] / [trailing] slot and a [face] mark
 * (`component-contract.md` § 7.2 R2).
 *
 * @property key Identity — the event carries it back (R1); [ChipSet] resolves it to whatever payload
 * dispatching the original action needs (a filter value, a tag, a category).
 */
@Immutable
data class ChipUiModel(
    val key: String,
    val label: String,
    val tone: ChipTone = ChipTone.Tonal,
    val selected: Boolean = false,
    val interaction: ChipInteraction = ChipInteraction.Clickable,
    val size: ChipSize = ChipSize.Regular,
    val leading: ChipLeading? = null,
    val trailing: ChipTrailing? = null,
    val face: ChipFace = ChipFace.Plain,
) {
    companion object : UiModelPreviews<ChipUiModel> {
        /**
         * Per R5: one fixture per [ChipTone], one per [ChipLeading] / [ChipTrailing] kind, plus
         * [ChipInteraction.Disabled], [ChipInteraction.Inert] and a label long enough to ellipsise.
         */
        override val previews: ImmutableList<ChipUiModel> = persistentListOf(
            ChipUiModel(
                key = "tonal-idle",
                label = "Fantasy",
            ),
            ChipUiModel(
                key = "tonal-selected",
                label = "Currently reading",
                selected = true,
            ),
            ChipUiModel(
                key = "choice-idle",
                label = "Grid · 2",
                tone = ChipTone.Choice,
            ),
            ChipUiModel(
                key = "choice-selected",
                label = "Ascending",
                tone = ChipTone.Choice,
                selected = true,
                trailing = ChipTrailing.Icon(
                    icon = SoftcoverIcon.ArrowDropDown,
                    description = "Ascending",
                ),
            ),
            ChipUiModel(
                key = "filled",
                label = "Winter reading",
                tone = ChipTone.Filled,
                leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
            ),
            ChipUiModel(
                key = "container",
                label = "On the list",
                tone = ChipTone.Container,
                trailing = ChipTrailing.Icon(
                    icon = SoftcoverIcon.Close,
                    description = "Remove from list",
                ),
            ),
            ChipUiModel(
                key = "outlined",
                label = "Second helpings",
                tone = ChipTone.Outlined,
                leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
            ),
            ChipUiModel(
                key = "dashed",
                label = "Add tags",
                tone = ChipTone.Dashed,
                leading = ChipLeading.Icon(icon = SoftcoverIcon.Add),
            ),
            ChipUiModel(
                key = "spoiler",
                label = "Contains a major death",
                tone = ChipTone.Spoiler,
            ),
            ChipUiModel(
                key = "spoiler-toggle-marked",
                label = "Contains a major death",
                size = ChipSize.Compact,
                leading = ChipLeading.SpoilerToggle(
                    marked = true,
                    label = "Marked as spoiler — tap to unmark",
                ),
                trailing = ChipTrailing.Dismiss(label = "Remove Contains a major death"),
                interaction = ChipInteraction.Inert,
            ),
            ChipUiModel(
                key = "spoiler-toggle-unmarked",
                label = "Cozy mystery",
                size = ChipSize.Compact,
                leading = ChipLeading.SpoilerToggle(
                    marked = false,
                    label = "Mark as spoiler",
                ),
                trailing = ChipTrailing.Dismiss(label = "Remove Cozy mystery"),
                interaction = ChipInteraction.Inert,
            ),
            ChipUiModel(
                key = "leading-icon",
                label = "Just now",
                leading = ChipLeading.Icon(icon = SoftcoverIcon.DateRange),
            ),
            ChipUiModel(
                key = "dismissible",
                label = "Yesterday, 14:30",
                selected = true,
                leading = ChipLeading.Icon(icon = SoftcoverIcon.DateRange),
                trailing = ChipTrailing.Dismiss(label = "Reset to just now"),
            ),
            ChipUiModel(
                key = "format-bold",
                label = "Bold",
                face = ChipFace.Bold,
            ),
            ChipUiModel(
                key = "disabled",
                label = "Cozy mystery",
                interaction = ChipInteraction.Disabled,
            ),
            ChipUiModel(
                key = "inert",
                label = "Cozy mystery",
                interaction = ChipInteraction.Inert,
            ),
            ChipUiModel(
                key = "long-label",
                label = "A Chip Label Long Enough That It Must Ellipsise Somewhere",
            ),
        )
    }
}
